//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include "jniUtils.h"
#include "rawTls.h"

#include <atomic>
#include <pthread.h>

namespace privmx {
    namespace wrapper {
        namespace jni {
            namespace {
                /**
                 * Guards the whole "is the VM still usable" decision.
                 *
                 * Readers (attach / detach) take a shared lock, shutdownThreadAttachManager()
                 * takes the exclusive one, so a detach which already passed the check always
                 * finishes before the VM is announced as gone.
                 *
                 * This MUST be a raw, statically initialized lock instead of std::shared_mutex.
                 * Endpoint stops its threads from static destructors, so a TLS destructor can
                 * run after the static destructors of this translation unit already executed -
                 * locking an std::shared_mutex at that point fails with EINVAL and
                 * "mutex lock failed: Invalid argument" terminates the process.
                 * PTHREAD_RWLOCK_INITIALIZER is a constant initializer, so this lock has no
                 * constructor and no destructor and stays valid until the process is gone.
                 */
                pthread_rwlock_t g_vmLock = PTHREAD_RWLOCK_INITIALIZER;

                struct SharedLock {
                    bool isLocked;

                    SharedLock() : isLocked(pthread_rwlock_rdlock(&g_vmLock) == 0) {}

                    ~SharedLock() { if (isLocked) pthread_rwlock_unlock(&g_vmLock); }

                    bool locked() const { return isLocked; }
                };

                struct ExclusiveLock {
                    bool isLocked;

                    ExclusiveLock() : isLocked(pthread_rwlock_wrlock(&g_vmLock) == 0) {}

                    ~ExclusiveLock() { if (isLocked) pthread_rwlock_unlock(&g_vmLock); }

                    bool locked() const { return isLocked; }
                };

                std::atomic<bool> g_initialized{false};
                /** Sticky - once the VM starts going down we never attach again. */
                std::atomic<bool> g_shuttingDown{false};

                /**
                 * Called by the TLS destructor when a natively created thread exits and
                 * by detachCurrentThreadIfAttached(). Never detaches a thread which was not
                 * attached by us - the TLS slot is set only for threads we attached.
                 */
                void detachThreadFromVM(void *value) {
                    if (value == nullptr) return;
                    auto *javaVM = reinterpret_cast<JavaVM *>(value);
                    SharedLock lock;
                    //could not take the lock - do not risk touching a VM which may be gone
                    if (!lock.locked()) return;
                    if (g_shuttingDown.load(std::memory_order_acquire)) return;
                    javaVM->DetachCurrentThread();
                }
            } // namespace

            void initThreadAttachManager(JavaVM *javaVM) {
                if (javaVM == nullptr) return;
                ExclusiveLock lock;
                if (g_initialized.load(std::memory_order_relaxed)) return;
                if (g_shuttingDown.load(std::memory_order_relaxed)) return;
                if (!rawTlsCreate(&detachThreadFromVM)) return;
                g_initialized.store(true, std::memory_order_release);
            }

            void shutdownThreadAttachManager() {
                ExclusiveLock lock;
                g_shuttingDown.store(true, std::memory_order_release);
            }

            JNIEnv *AttachCurrentThreadIfNeeded(
                    JavaVM *javaVM,
                    std::string shortThreadName,
                    jobject threadGroup
            ) {
                if (javaVM == nullptr) return nullptr;

                JNIEnv *jni = nullptr;
                jint status = javaVM->GetEnv((void **) &jni, JNI_VERSION_1_6);
                //return if current thread is attached
                if (jni != nullptr && status == JNI_OK) return jni;

                //lazy init for the case when JNI_OnLoad did not run (library loaded by another VM)
                if (!g_initialized.load(std::memory_order_acquire)) initThreadAttachManager(javaVM);

                SharedLock lock;
                if (!lock.locked()) return nullptr;
                //VM is being destroyed - callbacks into managed code are not possible anymore
                if (g_shuttingDown.load(std::memory_order_acquire)) return nullptr;
                if (!rawTlsExists()) return nullptr;

                std::string name(
                        shortThreadName +
                        std::to_string(
                                std::hash<std::thread::id>{}(std::this_thread::get_id())
                        )
                );
                JavaVMAttachArgs args;
                args.version = JNI_VERSION_1_6;
                args.name = &name[0];
                args.group = threadGroup;
#ifdef _JAVASOFT_JNI_H_  // Oracle's jni.h violates the JNI spec!
                void* env = nullptr;
#else
                JNIEnv *env = nullptr;
#endif

                //attach as daemon, so DestroyJavaVM does not wait for endpoint threads
                if (javaVM->AttachCurrentThreadAsDaemon(&env, &args) != JNI_OK) return nullptr;

                //TLS value is the detach marker - its destructor runs right before thread exit
                rawTlsSet(javaVM);
                return reinterpret_cast<JNIEnv *>(env);
            }

            void detachCurrentThreadIfAttached() {
                void *value = rawTlsGet();
                if (value == nullptr) return;
                rawTlsSet(nullptr);
                detachThreadFromVM(value);
            }
        } // jni
    } // wrapper
} // privmx

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    privmx::wrapper::jni::initThreadAttachManager(vm);
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *, void *) {
privmx::wrapper::jni::shutdownThreadAttachManager();
}

/**
 * Called from the JVM shutdown hook registered by LibLoader. JNI_OnUnload is not
 * guaranteed to run on DestroyJavaVM, so this is the reliable shutdown signal.
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_LibLoader_notifyVmShutdown(
        JNIEnv
*,
jobject
) {
privmx::wrapper::jni::shutdownThreadAttachManager();

}