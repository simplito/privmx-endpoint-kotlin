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
#include <privmx/endpoint/kvdb/KvdbApi.hpp>
#include "../utils.hpp"

#ifndef PRIVMXENDPOINT_KVDBAPI_H
#define PRIVMXENDPOINT_KVDBAPI_H

#endif //PRIVMXENDPOINT_KVDBAPI_H

privmx::endpoint::kvdb::KvdbApi *getKvdbApi(JniContextUtils &ctx, jobject kvdbApiInstance);
