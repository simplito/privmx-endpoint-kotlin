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

#include <jni.h>
#include <privmx/endpoint/lock/LockApi.hpp>
#include "../utils.hpp"

#ifndef PRIVMXENDPOINT_LOCKAPI_H
#define PRIVMXENDPOINT_LOCKAPI_H

#endif //PRIVMXENDPOINT_LOCKAPI_H

privmx::endpoint::lock::LockApi *getLockApi(JniContextUtils &ctx, jobject lockApiInstance);
