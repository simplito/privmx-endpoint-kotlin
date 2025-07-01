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

#ifndef PRIVMXENDPOINTWRAPPER_MODEL_NATIVE_INITIALIZERS_H
#define PRIVMXENDPOINTWRAPPER_MODEL_NATIVE_INITIALIZERS_H

#include <jni.h>
#include "utils.hpp"
#include "privmx/endpoint/core/Connection.hpp"
#include "privmx/endpoint/core/UserVerifierInterface.hpp"
#include "privmx/endpoint/core/Types.hpp"
#include "privmx/endpoint/core/Events.hpp"
#include "privmx/endpoint/thread/ThreadApi.hpp"
#include "privmx/endpoint/thread/Types.hpp"
#include "privmx/endpoint/thread/Events.hpp"
#include "privmx/endpoint/store/StoreApi.hpp"
#include "privmx/endpoint/store/Types.hpp"
#include "privmx/endpoint/store/Events.hpp"
#include "privmx/endpoint/inbox/InboxApi.hpp"
#include "privmx/endpoint/inbox/Types.hpp"
#include "privmx/endpoint/inbox/Events.hpp"
#include "privmx/endpoint/event/EventApi.hpp"
#include "privmx/endpoint/event/Types.hpp"
#include "privmx/endpoint/event/Events.hpp"
#include "privmx/endpoint/crypto/Types.hpp"
#include "privmx/endpoint/crypto/ExtKey.hpp"

namespace privmx {
    namespace wrapper {
        //Core
        jobject
        itemPolicy2Java(
                JniContextUtils &ctx,
                privmx::endpoint::core::ItemPolicy itemPolicy
        );
        
        jobject containerPolicyWithoutItem2Java(
                JniContextUtils &ctx,
                privmx::endpoint::core::ContainerPolicyWithoutItem containerPolicyWithoutItem
        );

        jobject containerPolicy2Java(
                JniContextUtils &ctx,
                privmx::endpoint::core::ContainerPolicy containerPolicy
        );

        //Context
        jobject context2Java(JniContextUtils &ctx, privmx::endpoint::core::Context context_c);

        // UserWithPubKey
        jobject userWithPubKey2Java(JniContextUtils &ctx,
                                    privmx::endpoint::core::UserWithPubKey userWithPubKey);

        //UserInfo
        jobject userInfo2Java(JniContextUtils &ctx, privmx::endpoint::core::UserInfo userInfo);

        jobject bridgeIdentity2Java(JniContextUtils &ctx,
                                    privmx::endpoint::core::BridgeIdentity bridgeIdentity_c);

        jobject verificationRequest2Java(JniContextUtils &ctx,
                                         privmx::endpoint::core::VerificationRequest verificationRequest_c);

        //Crypto
        jobject extKey2Java(JniContextUtils &ctx, privmx::endpoint::crypto::ExtKey extKey_c);

        jobject BIP392Java(JniContextUtils &ctx, privmx::endpoint::crypto::BIP39_t BIP39_c);

        //Threads
        jobject thread2Java(JniContextUtils &ctx, privmx::endpoint::thread::Thread thread_c);

        //Messages
        jobject serverMessageInfo2Java(JniContextUtils &ctx,
                                       privmx::endpoint::thread::ServerMessageInfo serverMessageInfo_c);

        jobject message2Java(JniContextUtils &ctx, privmx::endpoint::thread::Message message_c);

        //Store
        jobject store2Java(JniContextUtils &ctx, privmx::endpoint::store::Store store_c);

        //Inbox
        jobject
        filesConfig2Java(JniContextUtils &ctx, privmx::endpoint::inbox::FilesConfig filesConfig_c);

        jobject inbox2Java(JniContextUtils &ctx, privmx::endpoint::inbox::Inbox inbox_c);

        jobject
        inboxEntry2Java(JniContextUtils &ctx, privmx::endpoint::inbox::InboxEntry inboxEntry_c);

        jobject inboxPublicView2Java(JniContextUtils &ctx,
                                     privmx::endpoint::inbox::InboxPublicView inboxPublicView_c);

        //Files
        jobject serverFileInfo2Java(JniContextUtils &ctx,
                                    privmx::endpoint::store::ServerFileInfo serverFileInfo_c);

        jobject file2Java(JniContextUtils &ctx, privmx::endpoint::store::File file_c);

        //Event
        jobject storeDeletedEventData2Java(JniContextUtils &ctx,
                                           privmx::endpoint::store::StoreDeletedEventData storeDeletedEventData_c);

        jobject storeFileDeletedEventData2Java(JniContextUtils &ctx,
                                               privmx::endpoint::store::StoreFileDeletedEventData storeFileDeletedEventData_c);

        jobject storeStatsChangedEventData2Java(JniContextUtils &ctx,
                                                privmx::endpoint::store::StoreStatsChangedEventData storeStatsChangedEventData_c);

        jobject threadDeletedEventData2Java(JniContextUtils &ctx,
                                            privmx::endpoint::thread::ThreadDeletedEventData threadDeletedEventData_c);

        jobject threadDeletedMessageEventData2Java(JniContextUtils &ctx,
                                                   privmx::endpoint::thread::ThreadDeletedMessageEventData threadDeletedMessageEventData_c);

        jobject threadStatsEventData2Java(JniContextUtils &ctx,
                                          privmx::endpoint::thread::ThreadStatsEventData threadStatsEventData_c);

        jobject inboxDeletedEventData2Java(JniContextUtils &ctx,
                                           privmx::endpoint::inbox::InboxDeletedEventData inboxDeletedEventData_c);

        jobject inboxEntryDeletedEventData2Java(JniContextUtils &ctx,
                                                privmx::endpoint::inbox::InboxEntryDeletedEventData inboxEntryDeletedEventData_c);

        jobject contextCustomEventData2Java(JniContextUtils &ctx,
                                            privmx::endpoint::event::ContextCustomEventData contextCustomEvent_c
        );
    } // wrapper
} // privmx

#endif //PRIVMXENDPOINTWRAPPER_MODEL_NATIVE_INITIALIZERS_H
