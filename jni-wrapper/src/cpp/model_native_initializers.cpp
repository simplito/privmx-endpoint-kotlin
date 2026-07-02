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

#include "model_native_initializers.h"
#include "parser.h"

namespace privmx {
    namespace wrapper {
            //Core
            jobject
            itemPolicy2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::ItemPolicy itemPolicy
            ) {
                jclass itemPolicyCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ItemPolicy");
                jmethodID initItemPolicyMID = ctx->GetMethodID(
                        itemPolicyCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" // get
                        "Ljava/lang/String;" // listMy
                        "Ljava/lang/String;" // listAll
                        "Ljava/lang/String;" // create
                        "Ljava/lang/String;" // update
                        "Ljava/lang/String;" // delete
                        ")V"
                );
                jstring get = nullptr;
                jstring listMy = nullptr;
                jstring listAll = nullptr;
                jstring create = nullptr;
                jstring update = nullptr;
                jstring delete_ = nullptr;
                if (itemPolicy.get.has_value()) get = ctx->NewStringUTF(itemPolicy.get->c_str());
                if (itemPolicy.listMy.has_value())
                    listMy = ctx->NewStringUTF(itemPolicy.listMy->c_str());
                if (itemPolicy.listAll.has_value())
                    listAll = ctx->NewStringUTF(itemPolicy.listAll->c_str());
                if (itemPolicy.create.has_value())
                    create = ctx->NewStringUTF(itemPolicy.create->c_str());
                if (itemPolicy.update.has_value())
                    update = ctx->NewStringUTF(itemPolicy.update->c_str());
                if (itemPolicy.delete_.has_value())
                    delete_ = ctx->NewStringUTF(itemPolicy.delete_->c_str());
                return ctx->NewObject(
                        itemPolicyCls,
                        initItemPolicyMID,
                        get,
                        listMy,
                        listAll,
                        create,
                        update,
                        delete_
                );
            }

            jobject containerPolicyWithoutItem2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::ContainerPolicyWithoutItem containerPolicyWithoutItem
            ) {
                jclass containerPolicyWithoutItemCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ContainerPolicyWithoutItem");
                jmethodID initContainerPolicyWithoutItemMID = ctx->GetMethodID(
                        containerPolicyWithoutItemCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" // get
                        "Ljava/lang/String;" // update
                        "Ljava/lang/String;" // delete
                        "Ljava/lang/String;" // updatePolicy
                        "Ljava/lang/String;" // updaterCanBeRemovedFromManagers
                        "Ljava/lang/String;" // ownerCanBeRemovedFromManagers
                        ")V"
                );
                jstring get = nullptr;
                jstring update = nullptr;
                jstring delete_ = nullptr;
                jstring updatePolicy = nullptr;
                jstring updaterCanBeRemovedFromManagers = nullptr;
                jstring ownerCanBeRemovedFromManagers = nullptr;
                if (containerPolicyWithoutItem.get.has_value()) {
                    get = ctx->NewStringUTF(containerPolicyWithoutItem.get->c_str());
                }
                if (containerPolicyWithoutItem.update.has_value()) {
                    update = ctx->NewStringUTF(containerPolicyWithoutItem.update->c_str());
                }
                if (containerPolicyWithoutItem.delete_.has_value()) {
                    delete_ = ctx->NewStringUTF(containerPolicyWithoutItem.delete_->c_str());
                }
                if (containerPolicyWithoutItem.updatePolicy.has_value()) {
                    updatePolicy = ctx->NewStringUTF(containerPolicyWithoutItem.updatePolicy->c_str());
                }
                if (containerPolicyWithoutItem.updaterCanBeRemovedFromManagers.has_value()) {
                    updaterCanBeRemovedFromManagers = ctx->NewStringUTF(
                            containerPolicyWithoutItem.updaterCanBeRemovedFromManagers->c_str());
                }
                if (containerPolicyWithoutItem.ownerCanBeRemovedFromManagers.has_value()) {
                    ownerCanBeRemovedFromManagers = ctx->NewStringUTF(
                            containerPolicyWithoutItem.ownerCanBeRemovedFromManagers->c_str());
                }

                return ctx->NewObject(
                        containerPolicyWithoutItemCls,
                        initContainerPolicyWithoutItemMID,
                        get,
                        update,
                        delete_,
                        updatePolicy,
                        updaterCanBeRemovedFromManagers,
                        ownerCanBeRemovedFromManagers
                );
            }

            jobject containerPolicy2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::ContainerPolicy containerPolicy
            ) {
                jclass containerPolicyCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ContainerPolicy");
                jmethodID initContainerPolicyMID = ctx->GetMethodID(
                        containerPolicyCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" // get
                        "Ljava/lang/String;" // update
                        "Ljava/lang/String;" // delete
                        "Ljava/lang/String;" // updatePolicy
                        "Ljava/lang/String;" // updaterCanBeRemovedFromManagers
                        "Ljava/lang/String;" // ownerCanBeRemovedFromManagers
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ItemPolicy;" // item
                        ")V"
                );
                jstring get = nullptr;
                jstring update = nullptr;
                jstring delete_ = nullptr;
                jstring updatePolicy = nullptr;
                jstring updaterCanBeRemovedFromManagers = nullptr;
                jstring ownerCanBeRemovedFromManagers = nullptr;
                jobject itemPolicy = nullptr;
                if (containerPolicy.get.has_value()) {
                    get = ctx->NewStringUTF(containerPolicy.get->c_str());
                }
                if (containerPolicy.update.has_value()) {
                    update = ctx->NewStringUTF(containerPolicy.update->c_str());
                }
                if (containerPolicy.delete_.has_value()) {
                    delete_ = ctx->NewStringUTF(containerPolicy.delete_->c_str());
                }
                if (containerPolicy.updatePolicy.has_value()) {
                    updatePolicy = ctx->NewStringUTF(containerPolicy.updatePolicy->c_str());
                }
                if (containerPolicy.updaterCanBeRemovedFromManagers.has_value()) {
                    updaterCanBeRemovedFromManagers = ctx->NewStringUTF(
                            containerPolicy.updaterCanBeRemovedFromManagers->c_str());
                }
                if (containerPolicy.ownerCanBeRemovedFromManagers.has_value()) {
                    ownerCanBeRemovedFromManagers = ctx->NewStringUTF(
                            containerPolicy.ownerCanBeRemovedFromManagers->c_str());
                }
                if (containerPolicy.item.has_value()) {
                    itemPolicy = itemPolicy2Java(ctx, containerPolicy.item.value());
                }

                return ctx->NewObject(
                        containerPolicyCls,
                        initContainerPolicyMID,
                        get,
                        update,
                        delete_,
                        updatePolicy,
                        updaterCanBeRemovedFromManagers,
                        ownerCanBeRemovedFromManagers,
                        itemPolicy
                );
            }

            // CollectionItemChange
            jobject collectionItemChange2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::CollectionItemChange collectionItemChange_c
            ) {
                jclass collectionItemChangeCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/CollectionItemChange");
                jmethodID initCollectionItemChangeMID = ctx->GetMethodID(
                        collectionItemChangeCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"  // itemId
                        "Ljava/lang/String;"  // action
                        ")V"
                );
                return ctx->NewObject(
                        collectionItemChangeCls,
                        initCollectionItemChangeMID,
                        ctx->NewStringUTF(collectionItemChange_c.itemId.c_str()),
                        ctx->NewStringUTF(collectionItemChange_c.action.c_str())
                );
            }

            //Context
            jobject context2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::Context context_c
            ) {
                jclass contextCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Context");
                jmethodID initThreadDataMID = ctx->GetMethodID(
                        contextCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        contextCls,
                        initThreadDataMID,
                        ctx->NewStringUTF(context_c.userId.c_str()),
                        ctx->NewStringUTF(context_c.contextId.c_str())
                );
            }

            // UserWithPubKey
            jobject userWithPubKey2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::UserWithPubKey userWithPubKey
            ) {
                jclass userCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/UserWithPubKey");
                jmethodID initUserMID = ctx->GetMethodID(
                        userCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        userCls,
                        initUserMID,
                        ctx->NewStringUTF(userWithPubKey.userId.c_str()),
                        ctx->NewStringUTF(userWithPubKey.pubKey.c_str())
                );
            }

            // UserWithPubKey
            jobject userStatusChange2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::UserStatusChange userStatusChange
            ) {
                jclass userStatusCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/UserStatusChange");
                jmethodID initUserStatusMID = ctx->GetMethodID(
                        userStatusCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"    // action
                        "Ljava/lang/Long;"      // timestamp
                        ")V"
                );
                return ctx->NewObject(
                        userStatusCls,
                        initUserStatusMID,
                        ctx->NewStringUTF(userStatusChange.action.c_str()),
                        ctx.long2jLong(userStatusChange.timestamp)
                );
            }

            //UserInfo
            jobject userInfo2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::UserInfo userInfo
            ) {
                jclass userInfoCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/UserInfo");
                jmethodID initUserInfoMID = ctx->GetMethodID(
                        userInfoCls,
                        "<init>",
                        "("
                        "Lcom/simplito/kotlin/privmx_endpoint/model/UserWithPubKey;"      // userWithPubKey
                        "Z"                                                             // isActive
                        "Lcom/simplito/kotlin/privmx_endpoint/model/UserStatusChange;"    // lastStatusChange
                        ")V"
                );

                jobject userStatusChange = userInfo.lastStatusChange.has_value() ?
                        userStatusChange2Java(ctx, userInfo.lastStatusChange.value()) :
                        nullptr;
                return ctx->NewObject(
                        userInfoCls,
                        initUserInfoMID,
                        userWithPubKey2Java(ctx, userInfo.user),
                        (jboolean) userInfo.isActive,
                        userStatusChange);
            }

            // UserWithAction
            jobject userWithAction2Java(JniContextUtils &ctx,
                    privmx::endpoint::core::UserWithAction userWithAction) {
                jclass userWithActionCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/UserWithAction");
                jmethodID initUserWithActionMID = ctx->GetMethodID(
                        userWithActionCls,
                        "<init>",
                        "("
                        "Lcom/simplito/kotlin/privmx_endpoint/model/UserWithPubKey;"  // userWithPubKey
                        "Ljava/lang/String;"                                        // action
                        ")V"
                );
                return ctx->NewObject(
                        userWithActionCls,
                        initUserWithActionMID,
                        userWithPubKey2Java(ctx, userWithAction.user),
                        ctx->NewStringUTF(userWithAction.action.c_str())
                );
            }

            jobject bridgeIdentity2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::BridgeIdentity bridgeIdentity_c
            ) {
                jclass bridgeIdentityCls = ctx.findClass(
                        "com/simplito/kotlin/privmx_endpoint/model/BridgeIdentity");
                jmethodID initBridgeIdentityMID = ctx->GetMethodID(
                        bridgeIdentityCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"
                        "Ljava/lang/String;"
                        "Ljava/lang/String;"
                        ")V"
                );

                jstring pubKey_c = nullptr;
                if (bridgeIdentity_c.pubKey.has_value()) {
                    pubKey_c = ctx->NewStringUTF(bridgeIdentity_c.pubKey.value().c_str());
                }

                jstring instanceId_c = nullptr;
                if (bridgeIdentity_c.instanceId.has_value()) {
                    instanceId_c = ctx->NewStringUTF(bridgeIdentity_c.instanceId.value().c_str());
                }

                return ctx->NewObject(
                        bridgeIdentityCls,
                        initBridgeIdentityMID,
                        ctx->NewStringUTF(bridgeIdentity_c.url.c_str()),
                        pubKey_c,
                        instanceId_c
                );
            }

            jobject verificationRequest2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::VerificationRequest verificationRequest_c
            ) {
                jclass verificationRequestCls = ctx.findClass(
                        "com/simplito/kotlin/privmx_endpoint/model/VerificationRequest");
                jmethodID initVerificationRequestMID = ctx->GetMethodID(
                        verificationRequestCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"
                        "Ljava/lang/String;"
                        "Ljava/lang/String;"
                        "Ljava/lang/Long;"
                        "Lcom/simplito/kotlin/privmx_endpoint/model/BridgeIdentity;"
                        ")V"
                );

                jobject bridgeIdentity = nullptr;
                if (verificationRequest_c.bridgeIdentity.has_value()) {
                    bridgeIdentity = bridgeIdentity2Java(ctx,
                            verificationRequest_c.bridgeIdentity.value());
                }

                return ctx->NewObject(
                        verificationRequestCls,
                        initVerificationRequestMID,
                        ctx->NewStringUTF(verificationRequest_c.contextId.c_str()),
                        ctx->NewStringUTF(verificationRequest_c.senderId.c_str()),
                        ctx->NewStringUTF(verificationRequest_c.senderPubKey.c_str()),
                        ctx.long2jLong(verificationRequest_c.date),
                        bridgeIdentity
                );
            }

            //Crypto
            jobject extKey2Java(JniContextUtils &ctx, privmx::endpoint::crypto::ExtKey extKey_c) {
                jclass ExtKeyCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/modules/crypto/ExtKey");
                jmethodID initExtKeyMID = ctx->GetMethodID(
                        ExtKeyCls, "<init>", "(Ljava/lang/Long;)V");

                auto *key = new privmx::endpoint::crypto::ExtKey(extKey_c);
                return ctx->NewObject(
                        ExtKeyCls,
                        initExtKeyMID,
                        ctx.long2jLong((jlong) key));
            }

            jobject BIP392Java(JniContextUtils &ctx, privmx::endpoint::crypto::BIP39_t BIP39_c) {
                jclass BIP39Cls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/BIP39");
                jmethodID initBIP39MID = ctx->GetMethodID(
                        BIP39Cls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"                                            //mnemonic
                        "Lcom/simplito/kotlin/privmx_endpoint/modules/crypto/ExtKey;"     //Ecc Key
                        "[B"                                                            // BIP-39 entropy
                        ")V"
                );
                jbyteArray entropy = ctx->NewByteArray(BIP39_c.entropy.size());
                ctx->SetByteArrayRegion(entropy, 0, BIP39_c.entropy.size(),
                        (jbyte *) BIP39_c.entropy.data());

                return ctx->NewObject(
                        BIP39Cls,
                        initBIP39MID,
                        ctx->NewStringUTF(BIP39_c.mnemonic.c_str()),
                        extKey2Java(ctx, BIP39_c.ext_key),
                        entropy
                );
            }

            //Threads
            jobject thread2Java(JniContextUtils &ctx, privmx::endpoint::thread::Thread thread_c) {
                jclass threadCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Thread");
                jmethodID initThreadMID = ctx->GetMethodID(
                        threadCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"
                        "Ljava/lang/String;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/String;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/String;"
                        "Ljava/util/List;"
                        "Ljava/util/List;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        "[B"
                        "[B"
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ContainerPolicy;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        ")V"
                );
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jstring threadId = ctx->NewStringUTF(thread_c.threadId.c_str());
                jstring contextId = ctx->NewStringUTF(thread_c.contextId.c_str());
                jstring creator = ctx->NewStringUTF(thread_c.creator.c_str());
                jstring lastModifier = ctx->NewStringUTF(thread_c.lastModifier.c_str());
                jobject users = ctx->NewObject(arrayCls, initArrayMID);
                jobject managers = ctx->NewObject(arrayCls, initArrayMID);
                jbyteArray publicMeta = ctx->NewByteArray(thread_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(thread_c.privateMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, thread_c.publicMeta.size(),
                        (jbyte *) thread_c.publicMeta.data());
                ctx->SetByteArrayRegion(privateMeta, 0, thread_c.privateMeta.size(),
                        (jbyte *) thread_c.privateMeta.data());
                for (auto &user: thread_c.users) {
                    ctx->CallBooleanMethod(users,
                            addToArrayMID,
                            ctx->NewStringUTF(user.c_str()));
                }
                for (auto &manager: thread_c.managers) {
                    ctx->CallBooleanMethod(managers,
                            addToArrayMID,
                            ctx->NewStringUTF(manager.c_str()));
                }
                return ctx->NewObject(
                        threadCls,
                        initThreadMID,
                        contextId,
                        threadId,
                        ctx.long2jLong(thread_c.createDate),
                        creator,
                        ctx.long2jLong(thread_c.lastModificationDate),
                        lastModifier,
                        users,
                        managers,
                        ctx.long2jLong(thread_c.version),
                        ctx.long2jLong(thread_c.lastMsgDate),
                        publicMeta,
                        privateMeta,
                        containerPolicy2Java(ctx, thread_c.policy),
                        ctx.long2jLong(thread_c.messagesCount),
                        ctx.long2jLong(thread_c.statusCode),
                        ctx.long2jLong(thread_c.schemaVersion)
                );
            }

            //Messages
            jobject serverMessageInfo2Java(JniContextUtils &ctx,
                    privmx::endpoint::thread::ServerMessageInfo serverMessageInfo_c) {
                jclass messageCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ServerMessageInfo");
                jmethodID initMessageMID = ctx->GetMethodID(
                        messageCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        messageCls,
                        initMessageMID,
                        ctx->NewStringUTF(serverMessageInfo_c.threadId.c_str()),
                        ctx->NewStringUTF(serverMessageInfo_c.messageId.c_str()),
                        ctx.long2jLong(serverMessageInfo_c.createDate),
                        ctx->NewStringUTF(serverMessageInfo_c.author.c_str())
                );
            }

            jobject message2Java(JniContextUtils &ctx, privmx::endpoint::thread::Message message_c) {
                jclass messageCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Message");
                jmethodID initMessageMID = ctx->GetMethodID(
                        messageCls,
                        "<init>",
                        "(Lcom/simplito/kotlin/privmx_endpoint/model/ServerMessageInfo;"
                        "[B"
                        "[B"
                        "[B"
                        "Ljava/lang/String;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        ")V"
                );

                jbyteArray publicMeta = ctx->NewByteArray(message_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(message_c.privateMeta.size());
                jbyteArray data = ctx->NewByteArray(message_c.data.size());
                ctx->SetByteArrayRegion(publicMeta, 0, message_c.publicMeta.size(),
                        (jbyte *) message_c.publicMeta.data());

                ctx->SetByteArrayRegion(privateMeta, 0, message_c.privateMeta.size(),
                        (jbyte *) message_c.privateMeta.data());

                ctx->SetByteArrayRegion(data, 0, message_c.data.size(),
                        (jbyte *) message_c.data.data());

                return ctx->NewObject(
                        messageCls,
                        initMessageMID,
                        serverMessageInfo2Java(ctx, message_c.info),
                        publicMeta,
                        privateMeta,
                        data,
                        ctx->NewStringUTF(message_c.authorPubKey.c_str()),
                        ctx.long2jLong(message_c.statusCode),
                        ctx.long2jLong(message_c.schemaVersion)
                );
            }

            //Store
            jobject store2Java(JniContextUtils &ctx, privmx::endpoint::store::Store store_c) {
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );

                jclass storeCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Store");
                jmethodID initStoreMID = ctx->GetMethodID(
                        storeCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"  //storeId
                        "Ljava/lang/String;"  //contextId
                        "Ljava/lang/Long;"  //createDate
                        "Ljava/lang/String;"  //creator
                        "Ljava/lang/Long;"  //lastModificationDate
                        "Ljava/lang/Long;"  //lastFileDate
                        "Ljava/lang/String;"  //lastModifier
                        "Ljava/util/List;"  //users
                        "Ljava/util/List;"  //managers
                        "Ljava/lang/Long;"  //version
                        "[B" //publicMeta
                        "[B" //privateMeta
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ContainerPolicy;" //policy
                        "Ljava/lang/Long;"  //filesCount
                        "Ljava/lang/Long;"  //statusCode
                        "Ljava/lang/Long;"  //schemaVersion
                        ")V"
                );

                jobject users = ctx->NewObject(arrayCls, initArrayMID);
                jobject managers = ctx->NewObject(arrayCls, initArrayMID);
                jbyteArray publicMeta = ctx->NewByteArray(store_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(store_c.privateMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, store_c.publicMeta.size(),
                        (jbyte *) store_c.publicMeta.data());
                ctx->SetByteArrayRegion(privateMeta, 0, store_c.privateMeta.size(),
                        (jbyte *) store_c.privateMeta.data());
                for (auto &user: store_c.users) {
                    ctx->CallBooleanMethod(users,
                            addToArrayMID,
                            ctx->NewStringUTF(user.c_str()));
                }
                for (auto &manager: store_c.managers) {
                    ctx->CallBooleanMethod(managers,
                            addToArrayMID,
                            ctx->NewStringUTF(manager.c_str()));
                }

                return ctx->NewObject(
                        storeCls,
                        initStoreMID,
                        ctx->NewStringUTF(store_c.storeId.c_str()),
                        ctx->NewStringUTF(store_c.contextId.c_str()),
                        ctx.long2jLong(store_c.createDate),
                        ctx->NewStringUTF(store_c.creator.c_str()),
                        ctx.long2jLong(store_c.lastModificationDate),
                        ctx.long2jLong(store_c.lastFileDate),
                        ctx->NewStringUTF(store_c.lastModifier.c_str()),
                        users,
                        managers,
                        ctx.long2jLong(store_c.version),
                        publicMeta,
                        privateMeta,
                        containerPolicy2Java(ctx, store_c.policy),
                        ctx.long2jLong(store_c.filesCount),
                        ctx.long2jLong(store_c.statusCode),
                        ctx.long2jLong(store_c.schemaVersion)
                );
            }

            //Inbox
            jobject inbox2Java(JniContextUtils &ctx, privmx::endpoint::inbox::Inbox inbox_c) {
                jclass inboxCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Inbox");
                jmethodID initInboxMID = ctx->GetMethodID(
                        inboxCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" //inboxId
                        "Ljava/lang/String;" //contextId
                        "Ljava/lang/Long;" //createDate
                        "Ljava/lang/String;" //creator
                        "Ljava/lang/Long;" //lastModificationDate
                        "Ljava/lang/String;" //lastModifier
                        "Ljava/util/List;" //users
                        "Ljava/util/List;" //managers
                        "Ljava/lang/Long;" //version
                        "[B" //publicMeta
                        "[B" //privateMeta
                        "Lcom/simplito/kotlin/privmx_endpoint/model/FilesConfig;" //filesConfig
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ContainerPolicyWithoutItem;" //policy
                        "Ljava/lang/Long;" //statusCode
                        "Ljava/lang/Long;" //schemaVersion
                        ")V"
                );
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jobject users = ctx->NewObject(arrayCls, initArrayMID);
                jobject managers = ctx->NewObject(arrayCls, initArrayMID);
                jbyteArray publicMeta = ctx->NewByteArray(inbox_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(inbox_c.privateMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, inbox_c.publicMeta.size(),
                        (jbyte *) inbox_c.publicMeta.data());
                ctx->SetByteArrayRegion(privateMeta, 0, inbox_c.privateMeta.size(),
                        (jbyte *) inbox_c.privateMeta.data());
                for (auto &user: inbox_c.users) {
                    ctx->CallBooleanMethod(users,
                            addToArrayMID,
                            ctx->NewStringUTF(user.c_str()));
                }
                for (auto &manager: inbox_c.managers) {
                    ctx->CallBooleanMethod(managers,
                            addToArrayMID,
                            ctx->NewStringUTF(manager.c_str()));
                }

                jobject filesConfig = nullptr;
                if (inbox_c.filesConfig.has_value()) {
                    filesConfig = filesConfig2Java(ctx, inbox_c.filesConfig.value());
                }

                return ctx->NewObject(
                        inboxCls,
                        initInboxMID,
                        ctx->NewStringUTF(inbox_c.inboxId.c_str()),
                        ctx->NewStringUTF(inbox_c.contextId.c_str()),
                        ctx.long2jLong(inbox_c.createDate),
                        ctx->NewStringUTF(inbox_c.creator.c_str()),
                        ctx.long2jLong(inbox_c.lastModificationDate),
                        ctx->NewStringUTF(inbox_c.lastModifier.c_str()),
                        users,
                        managers,
                        ctx.long2jLong(inbox_c.version),
                        publicMeta,
                        privateMeta,
                        filesConfig,
                        containerPolicyWithoutItem2Java(ctx, inbox_c.policy),
                        ctx.long2jLong(inbox_c.statusCode),
                        ctx.long2jLong(inbox_c.schemaVersion)
                );
            }

            jobject
            inboxEntry2Java(JniContextUtils &ctx, privmx::endpoint::inbox::InboxEntry inboxEntry_c) {
                jclass inboxEntryCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/InboxEntry");
                jmethodID initEntryViewMID = ctx->GetMethodID(
                        inboxEntryCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" //entryId
                        "Ljava/lang/String;" //inboxId
                        "[B" //data
                        "Ljava/util/List;" //files
                        "Ljava/lang/String;" //authorPubKey
                        "Ljava/lang/Long;" // createDate
                        "Ljava/lang/Long;" // statusCode
                        "Ljava/lang/Long;" // schemaVersion
                        ")V"
                );
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jbyteArray data = ctx->NewByteArray(inboxEntry_c.data.size());
                ctx->SetByteArrayRegion(data, 0, inboxEntry_c.data.size(),
                        (jbyte *) inboxEntry_c.data.data());
                jobject files = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &file: inboxEntry_c.files) {
                    ctx->CallBooleanMethod(files,
                            addToArrayMID,
                            file2Java(ctx, file));
                }
                return ctx->NewObject(
                        inboxEntryCls,
                        initEntryViewMID,
                        ctx->NewStringUTF(inboxEntry_c.entryId.c_str()),
                        ctx->NewStringUTF(inboxEntry_c.inboxId.c_str()),
                        data,
                        files,
                        ctx->NewStringUTF(inboxEntry_c.authorPubKey.c_str()),
                        ctx.long2jLong(inboxEntry_c.createDate),
                        ctx.long2jLong(inboxEntry_c.statusCode),
                        ctx.long2jLong(inboxEntry_c.schemaVersion)
                );
            }

            jobject inboxPublicView2Java(JniContextUtils &ctx,
                    privmx::endpoint::inbox::InboxPublicView inboxPublicView_c) {
                jclass inboxPublicViewCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/InboxPublicView");
                jmethodID initInboxPublicViewMID = ctx->GetMethodID(
                        inboxPublicViewCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"
                        "Ljava/lang/Long;"
                        "[B"
                        ")V"
                );
                jbyteArray publicMeta = ctx->NewByteArray(inboxPublicView_c.publicMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, inboxPublicView_c.publicMeta.size(),
                        (jbyte *) inboxPublicView_c.publicMeta.data());
                return ctx->NewObject(
                        inboxPublicViewCls,
                        initInboxPublicViewMID,
                        ctx->NewStringUTF(inboxPublicView_c.inboxId.c_str()),
                        ctx.long2jLong(inboxPublicView_c.version),
                        publicMeta
                );
            }

            jobject
            filesConfig2Java(JniContextUtils &ctx, privmx::endpoint::inbox::FilesConfig filesConfig_c) {
                jclass filesConfigCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/FilesConfig");
                jmethodID initFilesConfigMID = ctx->GetMethodID(
                        filesConfigCls,
                        "<init>",
                        "("
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        ")V"
                );
                return ctx->NewObject(
                        filesConfigCls,
                        initFilesConfigMID,
                        ctx.long2jLong(filesConfig_c.minCount),
                        ctx.long2jLong(filesConfig_c.maxCount),
                        ctx.long2jLong(filesConfig_c.maxFileSize),
                        ctx.long2jLong(filesConfig_c.maxWholeUploadSize)
                );
            }

            //Files
            jobject serverFileInfo2Java(JniContextUtils &ctx,
                    privmx::endpoint::store::ServerFileInfo serverFileInfo_c) {
                jclass serverFileInfoCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ServerFileInfo");
                jmethodID initServerFileInfoMID = ctx->GetMethodID(
                        serverFileInfoCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        serverFileInfoCls,
                        initServerFileInfoMID,
                        ctx->NewStringUTF(serverFileInfo_c.storeId.c_str()),
                        ctx->NewStringUTF(serverFileInfo_c.fileId.c_str()),
                        ctx.long2jLong(serverFileInfo_c.createDate),
                        ctx->NewStringUTF(serverFileInfo_c.author.c_str())
                );
            }

            jobject file2Java(JniContextUtils &ctx, privmx::endpoint::store::File file_c) {
                jclass fileCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/File");
                jmethodID initFileMID = ctx->GetMethodID(
                        fileCls,
                        "<init>",
                        "("
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ServerFileInfo;"  // info
                        "[B"                    // publicMeta
                        "[B"                    // privateMeta
                        "Ljava/lang/Long;"      // size
                        "Ljava/lang/String;"    // authorPubKey
                        "Ljava/lang/Long;"      // statusCode
                        "Ljava/lang/Long;"      // schemaVersion
                        "Z"                     // randomWrite
                        ")V"
                );

                jbyteArray publicMeta = ctx->NewByteArray(file_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(file_c.privateMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, file_c.publicMeta.size(),
                        (jbyte *) file_c.publicMeta.data());

                ctx->SetByteArrayRegion(privateMeta, 0, file_c.privateMeta.size(),
                        (jbyte *) file_c.privateMeta.data());

                return ctx->NewObject(
                        fileCls,
                        initFileMID,
                        serverFileInfo2Java(ctx, file_c.info),
                        publicMeta,
                        privateMeta,
                        ctx.long2jLong(file_c.size),
                        ctx->NewStringUTF(file_c.authorPubKey.c_str()),
                        ctx.long2jLong(file_c.statusCode),
                        ctx.long2jLong(file_c.schemaVersion),
                        (jboolean) file_c.randomWrite
                );
            }

            jobject fileChange2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::store::FileChange file_change_c
            ) {
                jclass fileChangeCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/FileChange");

                jmethodID initFcMID = ctx->GetMethodID(
                        fileChangeCls,
                        "<init>",
                        "("
                        "Ljava/lang/Long;"
                        "Ljava/lang/Long;"
                        "Z"
                        ")V"
                );

                jobject javaPos = ctx.long2jLong(file_change_c.pos);
                jobject javaLength = ctx.long2jLong(file_change_c.length);
                jboolean javaTruncate = (jboolean) file_change_c.truncate;

                return ctx->NewObject(
                        fileChangeCls,
                        initFcMID,
                        javaPos,
                        javaLength,
                        javaTruncate
                );
            }

            //Event
            jobject contextUsersStatusChangedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::ContextUsersStatusChangedEventData contextUsersStatusChangedEventData_c
            ) {
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jclass ContextUsersStatusChangedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ContextUsersStatusChangedEventData");
                jmethodID initContextUsersStatusChangedEventDataMID = ctx->GetMethodID(
                        ContextUsersStatusChangedEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"        // contextId
                        "Ljava/util/List;"          // users
                        ")V"
                );

                jobject users = ctx->NewObject(arrayCls, initArrayMID);

                for (auto &user: contextUsersStatusChangedEventData_c.users) {
                    ctx->CallBooleanMethod(users,
                            addToArrayMID,
                            userWithAction2Java(ctx, user)
                    );
                }

                return ctx->NewObject(
                        ContextUsersStatusChangedEventDataCls,
                        initContextUsersStatusChangedEventDataMID,
                        ctx->NewStringUTF(contextUsersStatusChangedEventData_c.contextId.c_str()),
                        users
                );
            }

            jobject contextUserEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::ContextUserEventData contextUserEventData_c
            ) {
                jclass contextUserEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ContextUserEventData");
                jmethodID initContextUserEventDataMID = ctx->GetMethodID(
                        contextUserEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"                                          // contextId
                        "Lcom/simplito/kotlin/privmx_endpoint/model/UserWithPubKey;"    // user
                        ")V"
                );

                return ctx->NewObject(
                        contextUserEventDataCls,
                        initContextUserEventDataMID,
                        ctx->NewStringUTF(contextUserEventData_c.contextId.c_str()),
                        userWithPubKey2Java(ctx, contextUserEventData_c.user)
                );
            }

            // CollectionChangedEventData
            jobject collectionChangedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::core::CollectionChangedEventData collectionChangedEventData_c
            ) {
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jclass collectionChangedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/CollectionChangedEventData");
                jmethodID initCollectionChangedEventDataMID = ctx->GetMethodID(
                        collectionChangedEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"        // moduleType
                        "Ljava/lang/String;"        // moduleId
                        "Ljava/lang/Long;"          // affectedItemsCount
                        "Ljava/util/List;"          // items
                        ")V"
                );

                jobject items = ctx->NewObject(arrayCls, initArrayMID);

                for (auto &item: collectionChangedEventData_c.items) {
                    ctx->CallBooleanMethod(items,
                            addToArrayMID,
                            collectionItemChange2Java(ctx, item)
                    );
                }

                return ctx->NewObject(
                        collectionChangedEventDataCls,
                        initCollectionChangedEventDataMID,
                        ctx->NewStringUTF(collectionChangedEventData_c.moduleType.c_str()),
                        ctx->NewStringUTF(collectionChangedEventData_c.moduleId.c_str()),
                        ctx.long2jLong(collectionChangedEventData_c.affectedItemsCount),
                        items
                );
            }

            jobject storeFileDeletedEventData2Java(JniContextUtils &ctx,
                    privmx::endpoint::store::StoreFileDeletedEventData storeFileDeletedEventData_c) {
                jclass storeFileDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/StoreFileDeletedEventData");
                jmethodID initStoreFileDeletedEventDataMID = ctx->GetMethodID(
                        storeFileDeletedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        storeFileDeletedEventDataCls,
                        initStoreFileDeletedEventDataMID,
                        ctx->NewStringUTF(storeFileDeletedEventData_c.fileId.c_str()),
                        ctx->NewStringUTF(storeFileDeletedEventData_c.contextId.c_str()),
                        ctx->NewStringUTF(storeFileDeletedEventData_c.storeId.c_str())
                );
            }

            jobject storeStatsChangedEventData2Java(JniContextUtils &ctx,
                    privmx::endpoint::store::StoreStatsChangedEventData storeStatsChangedEventData_c) {
                jclass storeStatsChangedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/StoreStatsChangedEventData");
                jmethodID initStoreStatsChangedEventDataMID = ctx->GetMethodID(
                        storeStatsChangedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;)V"
                );
                return ctx->NewObject(
                        storeStatsChangedEventDataCls,
                        initStoreStatsChangedEventDataMID,
                        ctx->NewStringUTF(storeStatsChangedEventData_c.storeId.c_str()),
                        ctx->NewStringUTF(storeStatsChangedEventData_c.contextId.c_str()),
                        ctx.long2jLong(storeStatsChangedEventData_c.lastFileDate),
                        ctx.long2jLong(storeStatsChangedEventData_c.filesCount)
                );
            }

            jobject threadDeletedEventData2Java(JniContextUtils &ctx,
                    privmx::endpoint::thread::ThreadDeletedEventData threadDeletedEventData_c) {
                jclass threadDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ThreadDeletedEventData");
                jmethodID initThreadDeletedEventDataMID = ctx->GetMethodID(
                        threadDeletedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        threadDeletedEventDataCls,
                        initThreadDeletedEventDataMID,
                        ctx->NewStringUTF(threadDeletedEventData_c.threadId.c_str())
                );
            }

            jobject threadDeletedMessageEventData2Java(JniContextUtils &ctx,
                    privmx::endpoint::thread::ThreadDeletedMessageEventData threadDeletedMessageEventData) {
                jclass threadDeletedMessageEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ThreadDeletedMessageEventData");
                jmethodID initThreadDeletedMessageEventDataMID = ctx->GetMethodID(
                        threadDeletedMessageEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        threadDeletedMessageEventDataCls,
                        initThreadDeletedMessageEventDataMID,
                        ctx->NewStringUTF(threadDeletedMessageEventData.threadId.c_str()),
                        ctx->NewStringUTF(threadDeletedMessageEventData.messageId.c_str())
                );
            }

            jobject storeDeletedEventData2Java(JniContextUtils &ctx,
                    privmx::endpoint::store::StoreDeletedEventData storeDeletedEventData_c) {
                jclass storeDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/StoreDeletedEventData");
                jmethodID initStoreDeletedEventDataMID = ctx->GetMethodID(
                        storeDeletedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        storeDeletedEventDataCls,
                        initStoreDeletedEventDataMID,
                        ctx->NewStringUTF(storeDeletedEventData_c.storeId.c_str())
                );
            }

            jobject storeFileUpdatedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::store::StoreFileUpdatedEventData storeFileUpdatedEventData_c
            ) {
                jclass storeFileUpdatedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/StoreFileUpdatedEventData");
                jmethodID initStoreFileUpdatedEventDataMID = ctx->GetMethodID(
                        storeFileUpdatedEventDataCls,
                        "<init>",
                        "("
                        "Lcom/simplito/kotlin/privmx_endpoint/model/File;"
                        "Ljava/util/List;"
                        ")V"
                );

                jclass arrayListCls = ctx->FindClass("java/util/ArrayList");
                jmethodID arrayListInitMID = ctx->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID arrayListAddMID = ctx->GetMethodID(arrayListCls, "add",
                        "(Ljava/lang/Object;)Z");

                jobject changesList = ctx->NewObject(
                        arrayListCls,
                        arrayListInitMID
                );

                for (const auto &change: storeFileUpdatedEventData_c.changes) {
                    jobject javaFileChange = fileChange2Java(ctx, change);
                    ctx->CallBooleanMethod(changesList, arrayListAddMID, javaFileChange);
                }

                return ctx->NewObject(
                        storeFileUpdatedEventDataCls,
                        initStoreFileUpdatedEventDataMID,
                        file2Java(ctx, storeFileUpdatedEventData_c.file),
                        changesList
                );
            }

            jobject threadStatsEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::thread::ThreadStatsEventData threadStatsEventData_c
            ) {
                jclass threadStatsEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ThreadStatsEventData");
                jmethodID initThreadStatsEventDataMID = ctx->GetMethodID(
                        threadStatsEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/Long;Ljava/lang/Long;)V"
                );
                return ctx->NewObject(
                        threadStatsEventDataCls,
                        initThreadStatsEventDataMID,
                        ctx->NewStringUTF(threadStatsEventData_c.threadId.c_str()),
                        ctx.long2jLong(threadStatsEventData_c.lastMsgDate),
                        ctx.long2jLong(threadStatsEventData_c.messagesCount)
                );
            }

            jobject inboxDeletedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::inbox::InboxDeletedEventData inboxDeletedEventData_c
            ) {
                jclass inboxDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/InboxDeletedEventData");
                jmethodID initInboxDeletedEventDataMID = ctx->GetMethodID(
                        inboxDeletedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        inboxDeletedEventDataCls,
                        initInboxDeletedEventDataMID,
                        ctx->NewStringUTF(inboxDeletedEventData_c.inboxId.c_str())
                );
            }

            jobject inboxEntryDeletedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::inbox::InboxEntryDeletedEventData inboxEntryDeletedEventData_c
            ) {
                jclass inboxEntryDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/InboxEntryDeletedEventData");
                jmethodID initInboxEntryDeletedEventDataMID = ctx->GetMethodID(
                        inboxEntryDeletedEventDataCls,
                        "<init>",
                        "(Ljava/lang/String;Ljava/lang/String;)V"
                );
                return ctx->NewObject(
                        inboxEntryDeletedEventDataCls,
                        initInboxEntryDeletedEventDataMID,
                        ctx->NewStringUTF(inboxEntryDeletedEventData_c.inboxId.c_str()),
                        ctx->NewStringUTF(inboxEntryDeletedEventData_c.entryId.c_str())
                );
            }

            jobject contextCustomEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::event::ContextCustomEventData contextCustomEventData_c
            ) {
                jclass contextCustomEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/ContextCustomEventData");
                jmethodID initContextCustomEventDataMID = ctx->GetMethodID(
                        contextCustomEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"    // contextId
                        "Ljava/lang/String;"    // userId
                        "[B"                    // payload
                        "Ljava/lang/Long;"       // statusCode
                        "Ljava/lang/Long;"       // schemaVersion
                        ")V"
                );
                jbyteArray payload = ctx->NewByteArray(contextCustomEventData_c.payload.size());
                ctx->SetByteArrayRegion(payload, 0, contextCustomEventData_c.payload.size(),
                        (jbyte *) contextCustomEventData_c.payload.data());
                return ctx->NewObject(
                        contextCustomEventDataCls,
                        initContextCustomEventDataMID,
                        ctx->NewStringUTF(contextCustomEventData_c.contextId.c_str()),
                        ctx->NewStringUTF(contextCustomEventData_c.userId.c_str()),
                        payload,
                        ctx.long2jLong(contextCustomEventData_c.statusCode),
                        ctx.long2jLong(contextCustomEventData_c.schemaVersion)
                );
            }


            jobject kvdbDeletedEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::KvdbDeletedEventData kvdbDeletedEventData_c
            ) {
                jclass kvdbDeletedEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/KvdbDeletedEventData");
                jmethodID initKvdbDeletedEventDataMID = ctx->GetMethodID(
                        kvdbDeletedEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"    // kvdbId
                        ")V"
                );

                return ctx->NewObject(
                        kvdbDeletedEventDataCls,
                        initKvdbDeletedEventDataMID,
                        ctx->NewStringUTF(kvdbDeletedEventData_c.kvdbId.c_str())
                );
            }

            jobject kvdbStatsEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::KvdbStatsEventData kvdbStatsEventData_c
            ) {
                jclass kvdbStatsEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/KvdbStatsEventData");
                jmethodID initKvdbStatsEventDataMID = ctx->GetMethodID(
                        kvdbStatsEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"    // kvdbId
                        "Ljava/lang/Long;"      // lastEntryDate
                        "Ljava/lang/Long;"      // entries
                        ")V"
                );

                return ctx->NewObject(
                        kvdbStatsEventDataCls,
                        initKvdbStatsEventDataMID,
                        ctx->NewStringUTF(kvdbStatsEventData_c.kvdbId.c_str()),
                        ctx.long2jLong(kvdbStatsEventData_c.lastEntryDate),
                        ctx.long2jLong(kvdbStatsEventData_c.entries)
                );
            }

            jobject kvdbDeletedEntryEventData2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::KvdbDeletedEntryEventData kvdbDeletedEntryEventData_c
            ) {
                jclass kvdbDeletedEntryEventDataCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/events/KvdbDeletedEntryEventData");
                jmethodID initKvdbDeletedEntryEventDataMID = ctx->GetMethodID(
                        kvdbDeletedEntryEventDataCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;"    // kvdbId
                        "Ljava/lang/String;"    // kvdbEntryKey
                        ")V"
                );

                return ctx->NewObject(
                        kvdbDeletedEntryEventDataCls,
                        initKvdbDeletedEntryEventDataMID,
                        ctx->NewStringUTF(kvdbDeletedEntryEventData_c.kvdbId.c_str()),
                        ctx->NewStringUTF(kvdbDeletedEntryEventData_c.kvdbEntryKey.c_str())
                );
            }

            //Kvdb
            jobject kvdb2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::Kvdb kvdb_c
            ) {
                jclass kvdbCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/Kvdb");
                jmethodID initKvdbMID = ctx->GetMethodID(
                        kvdbCls,
                        "<init>",
                        "("
                        "Ljava/lang/String;" //contextId
                        "Ljava/lang/String;" //kvdbId
                        "Ljava/lang/Long;" //createDate
                        "Ljava/lang/String;" //creator
                        "Ljava/lang/Long;" //lastModificationDate
                        "Ljava/lang/String;" //lastModifier
                        "Ljava/util/List;" //users
                        "Ljava/util/List;" //managers
                        "Ljava/lang/Long;" //version
                        "[B" //publicMeta
                        "[B" //privateMeta
                        "Ljava/lang/Long;" //entries
                        "Ljava/lang/Long;" //lastEntryDate
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ContainerPolicy;" //policy
                        "Ljava/lang/Long;" //statusCode
                        "Ljava/lang/Long;" //schemaVersion
                        ")V"
                );
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "<init>",
                        "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(
                        arrayCls,
                        "add",
                        "(Ljava/lang/Object;)Z"
                );
                jobject users = ctx->NewObject(arrayCls, initArrayMID);
                jobject managers = ctx->NewObject(arrayCls, initArrayMID);
                jbyteArray publicMeta = ctx->NewByteArray(kvdb_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(kvdb_c.privateMeta.size());
                ctx->SetByteArrayRegion(publicMeta, 0, kvdb_c.publicMeta.size(),
                        (jbyte *) kvdb_c.publicMeta.data());
                ctx->SetByteArrayRegion(privateMeta, 0, kvdb_c.privateMeta.size(),
                        (jbyte *) kvdb_c.privateMeta.data());
                for (auto &user: kvdb_c.users) {
                    ctx->CallBooleanMethod(users,
                            addToArrayMID,
                            ctx->NewStringUTF(user.c_str()));
                }
                for (auto &manager: kvdb_c.managers) {
                    ctx->CallBooleanMethod(managers,
                            addToArrayMID,
                            ctx->NewStringUTF(manager.c_str()));
                }

                return ctx->NewObject(
                        kvdbCls,
                        initKvdbMID,
                        ctx->NewStringUTF(kvdb_c.contextId.c_str()),
                        ctx->NewStringUTF(kvdb_c.kvdbId.c_str()),
                        ctx.long2jLong(kvdb_c.createDate),
                        ctx->NewStringUTF(kvdb_c.creator.c_str()),
                        ctx.long2jLong(kvdb_c.lastModificationDate),
                        ctx->NewStringUTF(kvdb_c.lastModifier.c_str()),
                        users,
                        managers,
                        ctx.long2jLong(kvdb_c.version),
                        publicMeta,
                        privateMeta,
                        ctx.long2jLong(kvdb_c.entries),
                        ctx.long2jLong(kvdb_c.lastEntryDate),
                        containerPolicy2Java(ctx, kvdb_c.policy),
                        ctx.long2jLong(kvdb_c.statusCode),
                        ctx.long2jLong(kvdb_c.schemaVersion)
                );
            }

            jobject serverKvdbEntryInfo2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::ServerKvdbEntryInfo serverKvdbEntryInfo_c
            ) {
                jclass serverItemInfoCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/ServerKvdbEntryInfo");
                jmethodID initServerItemInfoMID = ctx->GetMethodID(
                        serverItemInfoCls,
                        "<init>",
                        "(Ljava/lang/String;"   // kvdbId
                        "Ljava/lang/String;"        // key
                        "Ljava/lang/Long;"          // createDate
                        "Ljava/lang/String;"        // author
                        ")V"
                );
                return ctx->NewObject(
                        serverItemInfoCls,
                        initServerItemInfoMID,
                        ctx->NewStringUTF(serverKvdbEntryInfo_c.kvdbId.c_str()),
                        ctx->NewStringUTF(serverKvdbEntryInfo_c.key.c_str()),
                        ctx.long2jLong(serverKvdbEntryInfo_c.createDate),
                        ctx->NewStringUTF(serverKvdbEntryInfo_c.author.c_str())
                );
            }

            jobject kvdbEntry2Java(
                    JniContextUtils &ctx,
                    privmx::endpoint::kvdb::KvdbEntry kvdbEntry_c
            ) {
                jclass itemCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/KvdbEntry");
                jmethodID initItemMID = ctx->GetMethodID(
                        itemCls,
                        "<init>",
                        "("
                        "Lcom/simplito/kotlin/privmx_endpoint/model/ServerKvdbEntryInfo;" // info
                        "[B"                    // publicMeta
                        "[B"                    // privateMeta
                        "[B"                    // data
                        "Ljava/lang/String;"    // authorPubKey
                        "Ljava/lang/Long;"      // version
                        "Ljava/lang/Long;"      // statusCode
                        "Ljava/lang/Long;"      // schemaVersion
                        ")V"
                );

                jbyteArray publicMeta = ctx->NewByteArray(kvdbEntry_c.publicMeta.size());
                jbyteArray privateMeta = ctx->NewByteArray(kvdbEntry_c.privateMeta.size());
                jbyteArray data = ctx->NewByteArray(kvdbEntry_c.data.size());

                ctx->SetByteArrayRegion(publicMeta, 0, kvdbEntry_c.publicMeta.size(),
                        (jbyte *) kvdbEntry_c.publicMeta.data());
                ctx->SetByteArrayRegion(privateMeta, 0, kvdbEntry_c.privateMeta.size(),
                        (jbyte *) kvdbEntry_c.privateMeta.data());
                ctx->SetByteArrayRegion(data, 0, kvdbEntry_c.data.size(),
                        (jbyte *) kvdbEntry_c.data.data());

                return ctx->NewObject(
                        itemCls,
                        initItemMID,
                        serverKvdbEntryInfo2Java(ctx, kvdbEntry_c.info),
                        publicMeta,
                        privateMeta,
                        data,
                        ctx->NewStringUTF(kvdbEntry_c.authorPubKey.c_str()),
                        ctx.long2jLong(kvdbEntry_c.version),
                        ctx.long2jLong(kvdbEntry_c.statusCode),
                        ctx.long2jLong(kvdbEntry_c.schemaVersion)
                );
            }


        //Streams
        jobject keyType2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::KeyType keyType_c
        ) {
            jclass keyTypeClass = ctx.findClass("com/simplito/kotlin/privmx_endpoint/model/stream/KeyType");
            jfieldID caseFieldId = nullptr;
            switch (keyType_c) {
                case privmx::endpoint::stream::KeyType::LOCAL:
                    caseFieldId = ctx->GetStaticFieldID(
                            keyTypeClass,
                            "LOCAL",
                            "Lcom/simplito/kotlin/privmx_endpoint/model/stream/KeyType;");
                    break;
                default:
                    caseFieldId = ctx->GetStaticFieldID(
                            keyTypeClass,
                            "REMOTE",
                            "Lcom/simplito/kotlin/privmx_endpoint/model/stream/KeyType;");
                    break;
            }
            return ctx->GetStaticObjectField(keyTypeClass, caseFieldId);
        }

        jobject key2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::Key key_c
        ) {
            jclass keyCls = ctx.findClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/Key");
            jmethodID initKeyMID = ctx->GetMethodID(
                    keyCls,
                    "<init>",
                    "(Ljava/lang/String;"
                    "[B"
                    "Lcom/simplito/kotlin/privmx_endpoint/model/stream/KeyType;"
                    ")V"
            );

            jbyteArray jKey = ctx->NewByteArray(key_c.key.size());
            ctx->SetByteArrayRegion(jKey, 0, key_c.key.size(), (jbyte *) key_c.key.data());

            return ctx->NewObject(
                    keyCls,
                    initKeyMID,
                    ctx->NewStringUTF(key_c.keyId.c_str()),
                    jKey,
                    keyType2Java(ctx, key_c.type)
            );
        }

        jobject stream2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::Stream stream_c
        ) {
            jclass streamCls = ctx.findClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/Stream");
            jmethodID initStreamMID = ctx->GetMethodID(
                    streamCls,
                    "<init>",
                    "("
                    "Ljava/lang/Long;"  //streamId
                    "Ljava/lang/String;"  //userId
                    ")V"
            );
            return ctx->NewObject(
                    streamCls,
                    initStreamMID,
                    ctx.long2jLong(stream_c.streamId),
                    ctx->NewStringUTF(stream_c.userId.c_str())
            );
        }

        jobject
        turnCredentials2Java(
                JniContextUtils &ctx,

                privmx::endpoint::stream::TurnCredentials turnCredentials_c
        ) {
            jclass turnCredentialsCls = ctx.findClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/TurnCredentials");
            jmethodID initTurnCredentialsMID = ctx->GetMethodID(
                    turnCredentialsCls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"  //url
                    "Ljava/lang/String;"  //username
                    "Ljava/lang/String;"  //password
                    "Ljava/lang/Long;"  //expirationTime
                    ")V"
            );
            return ctx->NewObject(
                    turnCredentialsCls,
                    initTurnCredentialsMID,
                    ctx->NewStringUTF(turnCredentials_c.url.c_str()),
                    ctx->NewStringUTF(turnCredentials_c.username.c_str()),
                    ctx->NewStringUTF(turnCredentials_c.password.c_str()),
                    ctx.long2jLong(turnCredentials_c.expirationTime)
            );
        }

        jobject
        sdpWithTypeModel2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::SdpWithTypeModel sdpWithTypeModel_c
        ) {
            jclass sdpWithTypeModelCls = ctx.findClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/SdpWithTypeModel");
            jmethodID initSdpWithTypeModelMID = ctx->GetMethodID(
                    sdpWithTypeModelCls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"  //url
                    "Ljava/lang/String;"  //username
                    ")V"
            );
            return ctx->NewObject(
                    sdpWithTypeModelCls,
                    initSdpWithTypeModelMID,
                    ctx->NewStringUTF(sdpWithTypeModel_c.sdp.c_str()),
                    ctx->NewStringUTF(sdpWithTypeModel_c.type.c_str())
            );
        }

        // Stream
        jobject streamRoom2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamRoom streamRoom_c
        ) {
            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/StreamRoom");
            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"    // contextId
                    "Ljava/lang/String;"    // streamRoomId
                    "Ljava/lang/Long;"      // createDate
                    "Ljava/lang/String;"    // creator
                    "Ljava/lang/Long;"      // lastModificationDate
                    "Ljava/lang/String;"    // lastModifier
                    "Ljava/util/List;"      // users
                    "Ljava/util/List;"      // managers
                    "Ljava/lang/Long;"      // version
                    "[B"                    // publicMeta
                    "[B"                    // privateMeta
                    "Lcom/simplito/kotlin/privmx_endpoint/model/ContainerPolicyWithoutItem;" // policy
                    "Ljava/lang/Long;"      // statusCode
                    "Ljava/lang/Long;"      // schemaVersion
                    "Ljava/lang/String;"    // state
                    ")V"
            );

            jbyteArray publicMeta = ctx->NewByteArray(streamRoom_c.publicMeta.size());
            jbyteArray privateMeta = ctx->NewByteArray(streamRoom_c.privateMeta.size());

            ctx->SetByteArrayRegion(publicMeta, 0, streamRoom_c.publicMeta.size(),
                    (jbyte *) streamRoom_c.publicMeta.data());
            ctx->SetByteArrayRegion(privateMeta, 0, streamRoom_c.privateMeta.size(),
                    (jbyte *) streamRoom_c.privateMeta.data());

            jobject users = vectorTojArray(ctx, streamRoom_c.users, string2jobject);
            jobject managers = vectorTojArray(ctx, streamRoom_c.managers, string2jobject);

            return ctx->NewObject(
                    itemCls,
                    initItemMID,
                    ctx->NewStringUTF(streamRoom_c.contextId.c_str()),
                    ctx->NewStringUTF(streamRoom_c.streamRoomId.c_str()),
                    ctx.long2jLong(streamRoom_c.createDate),
                    ctx->NewStringUTF(streamRoom_c.creator.c_str()),
                    ctx.long2jLong(streamRoom_c.lastModificationDate),
                    ctx->NewStringUTF(streamRoom_c.lastModifier.c_str()),
                    users,
                    managers,
                    ctx.long2jLong(streamRoom_c.version),
                    publicMeta,
                    privateMeta,
                    privmx::wrapper::containerPolicyWithoutItem2Java(ctx, streamRoom_c.policy),
                    ctx.long2jLong(streamRoom_c.statusCode),
                    ctx.long2jLong(streamRoom_c.schemaVersion),
                    ctx->NewStringUTF(streamRoom_c.state.c_str())
            );
        }


        jobject streamTrackInfo2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamTrackInfo streamTrackInfo_c
        ) {
            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/StreamTrackInfo");

            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"        // type
                    "J"                         // mindex
                    "Ljava/lang/String;"        // mid
                    "Z"                         // disabled
                    "Ljava/lang/String;"        // codec            [optional]
                    "Ljava/lang/String;"        // description      [optional]
                    "Z"                         // moderated
                    "Z"                         // simulcast
                    ")V"
            );

            jobject codec = nullptr;
            jobject description = nullptr;

            if (streamTrackInfo_c.codec.has_value()) {
                codec = ctx->NewStringUTF(streamTrackInfo_c.codec.value().c_str());
            }

            if (streamTrackInfo_c.description.has_value()) {
                description = ctx->NewStringUTF(streamTrackInfo_c.description.value().c_str());
            }

            return ctx->NewObject(
                    itemCls,
                    initItemMID,
                    ctx->NewStringUTF(streamTrackInfo_c.type.c_str()),
                    ctx.long2jLong(streamTrackInfo_c.mindex),
                    ctx->NewStringUTF(streamTrackInfo_c.mid.c_str()),
                    (jboolean) streamTrackInfo_c.disabled,
                    codec,
                    description,
                    (jboolean) streamTrackInfo_c.moderated,
                    (jboolean) streamTrackInfo_c.simulcast
            );
        }

        jobject
        streamInfo2Java(JniContextUtils &ctx, privmx::endpoint::stream::StreamInfo streamInfo_c) {
            jclass arrayCls = ctx->FindClass("java/util/ArrayList");
            jmethodID initArrayMID = ctx->GetMethodID(
                    arrayCls,
                    "<init>",
                    "()V");
            jmethodID addToArrayMID = ctx->GetMethodID(
                    arrayCls,
                    "add",
                    "(Ljava/lang/Object;)Z"
            );

            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/StreamInfo");

            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/Long;"          // id
                    "Ljava/lang/String;"        // userId
                    "Ljava/lang/String;"        // metadata
                    "Z"       // dummy
                    "Ljava/util/List;"          // tracks
                    ")V"
            );

            jobject metadata = nullptr;

            if (streamInfo_c.metadata.has_value()) {
                metadata = ctx->NewStringUTF(streamInfo_c.metadata.value().c_str());
            }

            jobject tracks = ctx->NewObject(arrayCls, initArrayMID);
            for (auto &track: streamInfo_c.tracks) {
                ctx->CallBooleanMethod(
                        tracks,
                        addToArrayMID,
                        streamTrackInfo2Java(ctx, track)
                );
            }

            // todo - check null?

            return ctx->NewObject(
                    itemCls,
                    initItemMID,
                    ctx.long2jLong(streamInfo_c.id),
                    ctx->NewStringUTF(streamInfo_c.userId.c_str()),
                    metadata,
                    (jboolean) streamInfo_c.dummy,
                    tracks
            );

        }

        jobject publishedStreamData2Java(JniContextUtils &ctx, privmx::endpoint::stream::PublishedStreamData publishedStreamData_c) {
            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/PublishedStreamData");

            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"      // streamRoomId
                    "Lcom/simplito/kotlin/privmx_endpoint/model/stream/StreamInfo;"      // stream
                    "Ljava/lang/String;"      //userId
                    ")V"
            );

            return ctx->NewObject(
                    itemCls,
                    initItemMID,
                    ctx->NewStringUTF(publishedStreamData_c.streamRoomId.c_str()),
                    streamInfo2Java(ctx, publishedStreamData_c.stream),
                    ctx->NewStringUTF(publishedStreamData_c.userId.c_str())
            );
        }

        jobject streamPublishResult2Java(JniContextUtils &ctx, privmx::endpoint::stream::StreamPublishResult streamPublishResult_c) {
            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/StreamPublishResult");

            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/Boolean;"      // published
                    "Lcom/simplito/kotlin/privmx_endpoint/model/stream/PublishedStreamData;"      // data
                    ")V"
            );

            jobject data = nullptr;
            if (streamPublishResult_c.data.has_value()) {
                data = publishedStreamData2Java(ctx, streamPublishResult_c.data.value());
                return ctx->NewObject(
                        itemCls,
                        initItemMID,
                        ctx.bool2jBoolean(streamPublishResult_c.published),
                        data
                );
            } else {
                return ctx->NewObject(
                        itemCls,
                        initItemMID,
                        ctx.bool2jBoolean(streamPublishResult_c.published)
                );
            }
        }

        jobject remoteStreamId2Java(JniContextUtils &ctx,
                privmx::endpoint::stream::RemoteStreamId remoteStreamId_c) {
            jclass itemCls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/RemoteStreamId");

            jmethodID initItemMID = ctx->GetMethodID(
                    itemCls,
                    "<init>",
                    "("
                    "Ljava/lang/Long;"      // value
                    ")V"
            );

            return ctx->NewObject(
                    itemCls,
                    initItemMID,
                    ctx.long2jLong(remoteStreamId_c)
            );
        }


        jobject
        streamTrackModificationPair2Java(
                JniContextUtils &ctx,
                endpoint::stream::StreamTrackModificationPair streamTrackModificationPair
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/StreamTrackModificationPair");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Lcom/privmx/stream/StreamTrackInfo;"
                    "Lcom/privmx/stream/StreamTrackInfo;"
                    ")V"
            );

            jobject before = streamTrackModificationPair.before
                    ? streamTrackInfo2Java(ctx, streamTrackModificationPair.before.value())
                    : nullptr;

            jobject after = streamTrackModificationPair.after
                    ? streamTrackInfo2Java(ctx, streamTrackModificationPair.after.value())
                    : nullptr;

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    before,
                    after
            );
        }


        jobject
        updatedStreamData2Java(
                JniContextUtils &ctx,
                endpoint::stream::UpdatedStreamData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/UpdatedStreamData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/lang/Long;"
                    "Ljava/lang/String;"
                    "Ljava/lang/Boolean;"
                    "Ljava/lang/Boolean;"
                    "Ljava/lang/String;"
                    "Ljava/lang/Long;"
                    "Ljava/lang/String;"
                    "Ljava/lang/String;"
                    ")V"
            );

            jobject jCodec = data.codec
                    ? ctx->NewStringUTF(data.codec->c_str())
                    : nullptr;

            jobject jStreamId = data.streamId
                    ? ctx.long2jLong(data.streamId.value())
                    : nullptr;

            jobject jStreamMid = data.streamMid
                    ? ctx->NewStringUTF(data.streamMid->c_str())
                    : nullptr;

            jobject jStreamDisplay = data.stream_display
                    ? ctx->NewStringUTF(data.stream_display->c_str())
                    : nullptr;

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.type.c_str()),
                    ctx.long2jLong(data.mindex),
                    ctx->NewStringUTF(data.mid.c_str()),
                    ctx.bool2jBoolean(data.send),
                    ctx.bool2jBoolean(data.ready),
                    jCodec,
                    jStreamId,
                    jStreamMid,
                    jStreamDisplay
            );
        }

        jobject streamRoomDeletedEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamRoomDeletedEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamRoomDeletedEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    ")V"
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str())
            );
        }

        jobject streamPublishedEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamPublishedEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamPublishedEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Lcom/simplito/kotlin/privmx_endpoint/model/stream/StreamInfo;"
                    "Ljava/lang/String;"
                    ")V"
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str()),
                    streamInfo2Java(ctx, data.stream),
                    ctx->NewStringUTF(data.userId.c_str())
            );
        }

        jobject streamUpdatedEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamUpdatedEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamUpdatedEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/util/List;"
                    "Ljava/util/List;"
                    "Ljava/util/List;"
                    ")V"
            );

            jobject addedList = vectorTojArray(
                    ctx,
                    data.streamsAdded,
                    streamInfo2Java
            );

            jobject removedList = vectorTojArray(
                    ctx,
                    data.streamsRemoved,
                    streamInfo2Java
            );

            jobject modifiedList = vectorTojArray(
                    ctx,
                    data.streamsModified,
                    streamTrackModification2Java
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str()),
                    addedList,
                    removedList,
                    modifiedList
            );
        }

        jobject
        streamEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/util/List;"
                    "Ljava/lang/String;"
                    ")V"
            );

            jobject streamIds = vectorTojArray(
                    ctx,
                    data.streamIds,
                    long2jobject
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str()),
                    streamIds,
                    ctx->NewStringUTF(data.userId.c_str())
            );
        }

        jobject
        streamLeftEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamLeftEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamLeftEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/lang/Long;"
                    "Ljava/lang/String;"
                    ")V"
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str()),
                    ctx.long2jLong(data.streamId),
                    ctx->NewStringUTF(data.userId.c_str())
            );
        }

        jobject
        streamUnpublishedEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamUnpublishedEventData data
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamUnpublishedEventData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/lang/Long;"
                    ")V"
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.streamRoomId.c_str()),
                    ctx.long2jLong(data.streamId)
            );
        }

        jobject
        streamsUpdated2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::StreamsUpdatedData data
        ) {
            jclass cls = ctx->FindClass("com/simplito/kotlin/privmx_endpoint/model/stream/events/StreamsUpdatedData");
            jmethodID initItemMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/String;"
                    "Ljava/util/List;"
                    ")V"
            );

            jobject streamsList = vectorTojArray(
                    ctx,
                    data.streams,
                    updatedStreamData2Java
            );

            return ctx->NewObject(
                    cls,
                    initItemMID,
                    ctx->NewStringUTF(data.room.c_str()),
                    streamsList
            );
        }

        jobject
        dataChannelMessage2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::DataChannelMessage message
        ) {
            jclass cls = ctx->FindClass("com/simplito/kotlin/privmx_endpoint/model/stream/DataChannelMessage");
            jmethodID initMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "[B"                    // message
                    "J"                     // seq
                    ")V"
            );

            jbyteArray data_c = ctx->NewByteArray(message.data.size());
            ctx->SetByteArrayRegion(
                    data_c, 0,
                    message.data.size(),
                    (jbyte *) message.data.data());

            return ctx->NewObject(
                    cls,
                    initMID,
                    data_c,
                    (jlong) message.seq
            );
        }

        jobject decryptedDataChannelMessage2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::DecryptedDataChannelMessage message
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/DecryptedDataChannelMessage");

            jmethodID initMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "J"                     // statusCode
                    "[B"                    // data
                    "J"                     // seq
                    ")V"
            );

            jbyteArray data_c = ctx->NewByteArray(message.data.size());
            ctx->SetByteArrayRegion(
                    data_c, 0,
                    message.data.size(),
                    (jbyte *) message.data.data());

            return ctx->NewObject(
                    cls,
                    initMID,
                    (jlong) message.statusCode,
                    data_c,
                    (jlong) message.seq
            );
        }

        jobject decryptedDataChannelMessage2Java(
                JniContextUtils &ctx,
                privmx::endpoint::stream::DecryptedDataChannelMessage message
        ) {
            jclass cls = ctx->FindClass(
                    "com/simplito/kotlin/privmx_endpoint/model/stream/events/DecryptedDataChannelMessage");

            jmethodID initMID = ctx->GetMethodID(
                    cls,
                    "<init>",
                    "("
                    "Ljava/lang/Long;"      // statusCode
                    "[B"                    // data
                    "Ljava/lang/Long;"      // seq
                    ")V"
            );

            jbyteArray data_c = ctx->NewByteArray(message.data.size());
            ctx->SetByteArrayRegion(
                    data_c, 0,
                    message.data.size(),
                    (jbyte *) message.data.data());

            return ctx->NewObject(
                    cls,
                    initMID,
                    ctx.long2jLong(message.statusCode),
                    data_c,
                    ctx.long2jLong(message.seq)
            );
        }

    } // wrapper
} // privmx