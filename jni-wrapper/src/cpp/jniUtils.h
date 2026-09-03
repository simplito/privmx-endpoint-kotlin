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

#ifndef PRIVMXENDPOINT_JNI_H
#define PRIVMXENDPOINT_JNI_H

#include "jni.h"
#include <string>
#include <thread>

namespace privmx {
    namespace wrapper {
        namespace jni {
            inline std::string getPrivmxCallbackThreadName() { return "privmx-callbacks"; }

            /**
             * Initializes thread attach manager. Called from JNI_OnLoad, idempotent.
             *
             * Creates the TLS key (pthread_key_create / FlsAlloc) whose destructor detaches
             * a natively created thread from the JVM right before that thread exits.
             *
             * @param javaVM pointer to JavaVM
             */
            void initThreadAttachManager(JavaVM *javaVM);

            /**
             * Marks the JavaVM as unusable.
             *
             * After this call no new thread is attached and no pending TLS destructor calls
             * DetachCurrentThread, so native threads outliving the JVM (endpoint stops its
             * threads during static destruction) cannot hang on a VM which is being destroyed.
             * Called from JNI_OnUnload and from the JVM shutdown hook.
             */
            void shutdownThreadAttachManager();

            /**
             * Attach current native thread to JVM if it is not attached.
             *
             * Threads are attached as daemons, so DestroyJavaVM never waits for them,
             * and are detached automatically by a TLS destructor when they exit.
             *
             * @param javaVM pointer to JavaVM
             * @param shortThreadName name of thread
             * @param threadGroup global ref of a ThreadGroup object or NULL
             * @return JNIEnv for attached thread or nullptr when the JVM is shutting down
             */
            JNIEnv *AttachCurrentThreadIfNeeded(
                    JavaVM *javaVM,
                    std::string shortThreadName,
                    jobject threadGroup = nullptr
            );

            /**
             * Detaches the current thread immediately, if it was attached by
             * AttachCurrentThreadIfNeeded(). Threads which were started from managed code
             * are left untouched.
             */
            void detachCurrentThreadIfAttached();
        } // jni
    } // wrapper
} // privmx

#endif //PRIVMXENDPOINT_JNI_H
