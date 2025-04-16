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

#ifndef PRIVMX_POCKET_LIB_PARSER_H
#define PRIVMX_POCKET_LIB_PARSER_H

#include "utils.hpp"

#include <jni.h>
#include "model_native_initializers.h"

std::vector<privmx::endpoint::core::UserWithPubKey>
usersToVector(JniContextUtils &ctx, jobjectArray users);

privmx::endpoint::core::ContainerPolicyWithoutItem
parseContainerPolicyWithoutItem(JniContextUtils &ctx, jobject containerPolicyWithoutItem);

privmx::endpoint::core::ContainerPolicy
parseContainerPolicy(JniContextUtils &ctx, jobject containerPolicy);

privmx::endpoint::core::ItemPolicy parseItemPolicy(JniContextUtils &ctx, jobject itemPolicy);

privmx::endpoint::inbox::FilesConfig parseFilesConfig(JniContextUtils &ctx, jobject filesConfig);

jobject parseEvent(JniContextUtils &ctx, std::shared_ptr<privmx::endpoint::core::Event> event);


#endif //PRIVMX_POCKET_LIB_PARSER_H
