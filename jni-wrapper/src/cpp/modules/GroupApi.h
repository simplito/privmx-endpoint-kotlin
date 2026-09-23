//
// PrivMX Endpoint Kotlin.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

#include <jni.h>
#include <optional>
#include <privmx/endpoint/group/GroupApi.hpp>
#include "../utils.hpp"

#ifndef PRIVMXENDPOINT_GROUPAPI_H
#define PRIVMXENDPOINT_GROUPAPI_H

#endif //PRIVMXENDPOINT_GROUPAPI_H

privmx::endpoint::group::GroupApi *getGroupApi(JniContextUtils &ctx, jobject groupApiInstance);

/**
 * Reads an optional `GroupApi` argument. A null `groupApiInstance` yields `std::nullopt`, which is what the
 * endpoint's `create` functions expect for a Group-unaware API.
 */
std::optional<privmx::endpoint::group::GroupApi>
getOptionalGroupApi(JniContextUtils &ctx, jobject groupApiInstance);
