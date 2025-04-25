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
#include <privmx/endpoint/store/StoreApi.hpp>
#include "../utils.hpp"

#ifndef PRIVMXENDPOINT_STOREAPI_H
#define PRIVMXENDPOINT_STOREAPI_H

#endif //PRIVMXENDPOINT_STOREAPI_H

privmx::endpoint::store::StoreApi *getStoreApi(JniContextUtils &ctx, jobject storeApiInstance);