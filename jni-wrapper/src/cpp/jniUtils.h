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
             * Attach current native thread to JVM if it is not attached.
             *
             * @param javaVM pointer to JavaVM
             * @param shortThreadName name of thread
             * @param threadGroup global ref of a ThreadGroup object or NULL
             * @return JNIEnv for attached thread
             */
            JNIEnv *AttachCurrentThreadIfNeeded(
                    JavaVM *javaVM,
                    std::string shortThreadName,
                    jobject threadGroup = nullptr
            );
        } // jni
    } // wrapper
} // privmx

#endif //PRIVMXENDPOINT_JNI_H
