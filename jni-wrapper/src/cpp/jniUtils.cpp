//
// Created by Dawid Jenczewski on 16/04/2025.
//

#include "jniUtils.h"

namespace privmx {
    namespace wrapper {
        namespace jni {
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
                    //Create tls object which detach thread from JVM when this thread exits
                    thread_local struct DetachJniOnExit {
                        JavaVM *javaVm;

                        DetachJniOnExit(JavaVM *javaVm1) {
                            javaVm = javaVm1;
                        }

                        ~DetachJniOnExit() {
                            javaVm->DetachCurrentThread();
                        }
                    } detachJniOnExit(javaVM);
                    return reinterpret_cast<JNIEnv *>(env);
                }
                return nullptr;
            }
        } // jni
    } // wrapper
} // privmx