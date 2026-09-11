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

#include <pthread.h>

namespace privmx {
    namespace wrapper {
        namespace jni {
            namespace {
                pthread_key_t detachJniKey;
                pthread_once_t detachJniKeyOnce = PTHREAD_ONCE_INIT;
                bool detachJniKeyCreated = false;

                //Called by pthread when a thread attached here exits
                void detachJniOnThreadExit(void *javaVM) {
                    if (javaVM != nullptr) static_cast<JavaVM *>(javaVM)->DetachCurrentThread();
                }

                void createDetachJniKey() {
                    detachJniKeyCreated =
                            pthread_key_create(&detachJniKey, detachJniOnThreadExit) == 0;
                }
            }

            JNIEnv *AttachCurrentThreadIfNeeded(
                    JavaVM *javaVM,
                    std::string shortThreadName,
                    jobject threadGroup
            ) {
                JNIEnv *jni = nullptr;
                jint status = javaVM->GetEnv((void **) &jni, JNI_VERSION_1_6);
                //return if current thread is attached
                if (jni != nullptr && status == JNI_OK) return jni;

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

                if (javaVM->AttachCurrentThread(&env, &args) == JNI_OK) {
                    //Register tls value which detach thread from JVM when this thread exits
                    pthread_once(&detachJniKeyOnce, createDetachJniKey);
                    if (detachJniKeyCreated) pthread_setspecific(detachJniKey, javaVM);
                    return reinterpret_cast<JNIEnv *>(env);
                }
                return nullptr;
            }
        } // jni
    } // wrapper
} // privmx