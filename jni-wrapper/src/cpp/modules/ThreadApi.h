//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include <jni.h>
#include <privmx/endpoint/thread/ThreadApi.hpp>
#include "../utils.hpp"

#ifndef PRIVMXENDPOINT_THREADAPI_H
#define PRIVMXENDPOINT_THREADAPI_H

#endif //PRIVMXENDPOINT_THREADAPI_H

privmx::endpoint::thread::ThreadApi *getThreadApi(JniContextUtils &ctx, jobject threadApiInstance);