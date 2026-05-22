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

#include "parser.h"

using namespace privmx::endpoint;

std::vector<privmx::endpoint::core::UserWithPubKey>
usersToVector(JniContextUtils &ctx, jobjectArray users) {
    std::vector<privmx::endpoint::core::UserWithPubKey> users_c;
    for (int i = 0; i < ctx->GetArrayLength(users); i++) {

        jobject arrayElement = ctx->GetObjectArrayElement(users, i);
        jclass arrayElementCls = ctx->GetObjectClass(arrayElement);

        jfieldID pubKeyFID = ctx->GetFieldID(arrayElementCls, "pubKey", "Ljava/lang/String;");
        jfieldID userIdFID = ctx->GetFieldID(arrayElementCls, "userId", "Ljava/lang/String;");
        privmx::endpoint::core::UserWithPubKey user = privmx::endpoint::core::UserWithPubKey();
        user.userId = ctx.jString2string(
                (jstring) ctx->GetObjectField(arrayElement, userIdFID));
        user.pubKey = ctx.jString2string(
                (jstring) ctx->GetObjectField(arrayElement, pubKeyFID));

        users_c.push_back(user);
    }
    return users_c;
}

privmx::endpoint::core::PKIVerificationOptions
parsePKIVerificationOptions(JniContextUtils &ctx, jobject pkiVerificationOptions) {
    auto result = privmx::endpoint::core::PKIVerificationOptions();
    if (pkiVerificationOptions == nullptr) return result;

    jclass pkiVerificationOptionsClass = ctx->GetObjectClass(pkiVerificationOptions);
    jfieldID bridgePubKey = ctx->GetFieldID(
            pkiVerificationOptionsClass,
            "bridgePubKey",
            "Ljava/lang/String;");
    jfieldID bridgeInstanceId = ctx->GetFieldID(
            pkiVerificationOptionsClass,
            "bridgeInstanceId",
            "Ljava/lang/String;");

    jstring value;
    if ((value = (jstring) ctx->GetObjectField(pkiVerificationOptions, bridgePubKey)) != NULL) {
        result.bridgePubKey = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(pkiVerificationOptions, bridgeInstanceId)) != NULL) {
        result.bridgeInstanceId = ctx.jString2string(value);
    }

    return result;
}

privmx::endpoint::core::ContainerPolicyWithoutItem
parseContainerPolicyWithoutItem(JniContextUtils &ctx, jobject containerPolicyWithoutItem) {
    auto result = privmx::endpoint::core::ContainerPolicyWithoutItem();
    if (containerPolicyWithoutItem == nullptr) return result;
    jclass policyClass = ctx->GetObjectClass(containerPolicyWithoutItem);
    jfieldID get = ctx->GetFieldID(policyClass, "get", "Ljava/lang/String;");
    jfieldID update = ctx->GetFieldID(policyClass, "update", "Ljava/lang/String;");
    jfieldID delete_ = ctx->GetFieldID(policyClass, "delete", "Ljava/lang/String;");
    jfieldID updatePolicy = ctx->GetFieldID(policyClass, "updatePolicy", "Ljava/lang/String;");
    jfieldID updaterCanBeRemovedFromManagers = ctx->GetFieldID(policyClass,
            "updaterCanBeRemovedFromManagers",
            "Ljava/lang/String;");
    jfieldID ownerCanBeRemovedFromManagers = ctx->GetFieldID(policyClass,
            "ownerCanBeRemovedFromManagers",
            "Ljava/lang/String;");
    jstring value;
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem, get)) != NULL) {
        result.get = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem, update)) != NULL) {
        result.update = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem, delete_)) != NULL) {
        result.delete_ = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem, updatePolicy)) != NULL) {
        result.updatePolicy = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem,
            updaterCanBeRemovedFromManagers)) != NULL) {
        result.updaterCanBeRemovedFromManagers = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicyWithoutItem,
            ownerCanBeRemovedFromManagers)) != NULL) {
        result.ownerCanBeRemovedFromManagers = ctx.jString2string(value);
    }
    return result;
}

privmx::endpoint::core::ContainerPolicy
parseContainerPolicy(JniContextUtils &ctx, jobject containerPolicy) {
    auto result = privmx::endpoint::core::ContainerPolicy();
    if (containerPolicy == nullptr) return result;

    jclass policyClass = ctx->GetObjectClass(containerPolicy);
    jfieldID get = ctx->GetFieldID(policyClass, "get", "Ljava/lang/String;");
    jfieldID update = ctx->GetFieldID(policyClass, "update", "Ljava/lang/String;");
    jfieldID delete_ = ctx->GetFieldID(policyClass, "delete", "Ljava/lang/String;");
    jfieldID updatePolicy = ctx->GetFieldID(policyClass, "updatePolicy", "Ljava/lang/String;");
    jfieldID updaterCanBeRemovedFromManagers = ctx->GetFieldID(policyClass,
            "updaterCanBeRemovedFromManagers",
            "Ljava/lang/String;");
    jfieldID ownerCanBeRemovedFromManagers = ctx->GetFieldID(policyClass,
            "ownerCanBeRemovedFromManagers",
            "Ljava/lang/String;");

    jfieldID item = ctx->GetFieldID(policyClass,
            "item",
            "Lcom/simplito/kotlin/privmx_endpoint/model/ItemPolicy;");
    jstring value;
    if ((value = (jstring) ctx->GetObjectField(containerPolicy, get)) != NULL) {
        result.get = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicy, update)) != NULL) {
        result.update = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicy, delete_)) != NULL) {
        result.delete_ = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicy, updatePolicy)) != NULL) {
        result.updatePolicy = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicy,
            updaterCanBeRemovedFromManagers)) != NULL) {
        result.updaterCanBeRemovedFromManagers = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(containerPolicy,
            ownerCanBeRemovedFromManagers)) != NULL) {
        result.ownerCanBeRemovedFromManagers = ctx.jString2string(value);
    }
    result.item = parseItemPolicy(ctx, ctx->GetObjectField(containerPolicy, item));
    return result;
}

privmx::endpoint::core::ItemPolicy
parseItemPolicy(JniContextUtils &ctx, jobject itemPolicy) {
    auto result = privmx::endpoint::core::ItemPolicy();
    if (itemPolicy == nullptr) return result;
    jclass policyClass = ctx->GetObjectClass(itemPolicy);
    jfieldID get = ctx->GetFieldID(policyClass, "get", "Ljava/lang/String;");
    jfieldID listMy = ctx->GetFieldID(policyClass, "listMy", "Ljava/lang/String;");
    jfieldID listAll = ctx->GetFieldID(policyClass, "listAll", "Ljava/lang/String;");
    jfieldID create = ctx->GetFieldID(policyClass, "create", "Ljava/lang/String;");
    jfieldID update = ctx->GetFieldID(policyClass, "update", "Ljava/lang/String;");
    jfieldID delete_ = ctx->GetFieldID(policyClass, "delete", "Ljava/lang/String;");

    jstring value;
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, get)) != NULL) {
        result.get = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, listMy)) != NULL) {
        result.listMy = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, listAll)) != NULL) {
        result.listAll = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, create)) != NULL) {
        result.create = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, update)) != NULL) {
        result.update = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(itemPolicy, delete_)) != NULL) {
        result.delete_ = ctx.jString2string(value);
    }
    return result;
}

privmx::endpoint::inbox::FilesConfig parseFilesConfig(JniContextUtils &ctx, jobject filesConfig) {
    auto result = privmx::endpoint::inbox::FilesConfig();
    jclass filesConfigCls = ctx->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/FilesConfig");
    jfieldID minCountFID = ctx->GetFieldID(filesConfigCls, "minCount", "Ljava/lang/Long;");
    jfieldID maxCountFID = ctx->GetFieldID(filesConfigCls, "maxCount", "Ljava/lang/Long;");
    jfieldID maxFileSizeFID = ctx->GetFieldID(filesConfigCls, "maxFileSize", "Ljava/lang/Long;");
    jfieldID maxWholeUploadSizeFID = ctx->GetFieldID(filesConfigCls, "maxWholeUploadSize",
            "Ljava/lang/Long;");
    result.minCount = ctx.getObject(ctx->GetObjectField(filesConfig, minCountFID)).getLongValue();
    result.maxCount = ctx.getObject(ctx->GetObjectField(filesConfig, maxCountFID)).getLongValue();
    result.maxFileSize = ctx.getObject(
            ctx->GetObjectField(filesConfig, maxFileSizeFID)).getLongValue();
    result.maxWholeUploadSize = ctx.getObject(
            ctx->GetObjectField(filesConfig, maxWholeUploadSizeFID)).getLongValue();
    return result;
}

jobject initEvent(
        JniContextUtils &ctx,
        std::string type,
        std::string channel,
        int64_t connectionId,
        std::vector <std::string> &subscriptions,
        int64_t timestamp,
        jobject data_j
) {
    if (type.empty()) return nullptr;
    jclass eventCls = ctx->FindClass("com/simplito/kotlin/privmx_endpoint/model/Event");
    jclass arrayCls = ctx->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = ctx->GetMethodID(arrayCls, "<init>", "()V");
    jmethodID addToArrayMID = ctx->GetMethodID(arrayCls, "add", "(Ljava/lang/Object;)Z");

    jobject subs = ctx->NewObject(arrayCls, initArrayMID);
    for (const auto &subscription: subscriptions) {
        jstring jSub = ctx->NewStringUTF(subscription.c_str());
        ctx->CallBooleanMethod(subs, addToArrayMID, jSub);
    }

    jmethodID eventInitMID = ctx->GetMethodID(
            eventCls,
            "<init>",
            "("
            "Ljava/lang/String;"   // type
            "Ljava/lang/String;"   // channel
            "Ljava/lang/Long;"     // connectionId
            "Ljava/util/List;"     // subscriptions
            "Ljava/lang/Long;"     // timestamp
            "Ljava/lang/Object;"   // data
            ")V"
    );

    return ctx->NewObject(
            eventCls,
            eventInitMID,
            ctx->NewStringUTF(type.c_str()),
            ctx->NewStringUTF(channel.c_str()),
            ctx.long2jLong(connectionId),
            subs,
            ctx.long2jLong(timestamp),
            data_j
    );
}

jobject
parseEvent(JniContextUtils &ctx, std::shared_ptr<privmx::endpoint::core::Event> event) {
    try {
        if (event::Events::isContextCustomEvent(event)) {
            privmx::endpoint::event::ContextCustomEvent event_cast = event::Events::extractContextCustomEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::contextCustomEventData2Java(ctx, event_cast.data)
            );
        } else if (core::Events::isCollectionChangedEvent(event)) {
            privmx::endpoint::core::CollectionChangedEvent event_cast = core::Events::extractCollectionChangedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::collectionChangedEventData2Java(ctx, event_cast.data)
            );
        } else if (core::Events::isContextUserAddedEvent(event)) {
            privmx::endpoint::core::ContextUserAddedEvent event_cast = core::Events::extractContextUserAddedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::contextUserEventData2Java(ctx, event_cast.data)
            );
        } else if (core::Events::isContextUserRemovedEvent(event)) {
            privmx::endpoint::core::ContextUserRemovedEvent event_cast = core::Events::extractContextUserRemovedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::contextUserEventData2Java(ctx, event_cast.data)
            );
        } else if (core::Events::isContextUsersStatusChangedEvent(event)) {
            privmx::endpoint::core::ContextUsersStatusChangedEvent event_cast = core::Events::extractContextUsersStatusChangedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::contextUsersStatusChangedEventData2Java(ctx, event_cast.data)
            );
        } else if (thread::Events::isThreadCreatedEvent(event)) {
            privmx::endpoint::thread::ThreadCreatedEvent event_cast = thread::Events::extractThreadCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::thread2Java(ctx, event_cast.data)
            );
        } else if (thread::Events::isThreadUpdatedEvent(event)) {
            privmx::endpoint::thread::ThreadUpdatedEvent event_cast = thread::Events::extractThreadUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::thread2Java(ctx, event_cast.data)
            );
        } else if (thread::Events::isThreadStatsEvent(event)) {
            privmx::endpoint::thread::ThreadStatsChangedEvent event_cast = thread::Events::extractThreadStatsEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::threadStatsEventData2Java(ctx, event_cast.data)
            );
        } else if (thread::Events::isThreadDeletedEvent(event)) {
            privmx::endpoint::thread::ThreadDeletedEvent event_cast = thread::Events::extractThreadDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::threadDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (thread::Events::isThreadNewMessageEvent(event)) {
            privmx::endpoint::thread::ThreadNewMessageEvent event_cast = thread::Events::extractThreadNewMessageEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::message2Java(ctx, event_cast.data)
            );
            return nullptr;
        } else if (thread::Events::isThreadMessageUpdatedEvent(event)) {
            privmx::endpoint::thread::ThreadMessageUpdatedEvent event_cast = thread::Events::extractThreadMessageUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::message2Java(ctx, event_cast.data)
            );
            return nullptr;
        } else if (thread::Events::isThreadMessageDeletedEvent(event)) {
            privmx::endpoint::thread::ThreadMessageDeletedEvent event_cast = thread::Events::extractThreadMessageDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::threadDeletedMessageEventData2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreCreatedEvent(event)) {
            privmx::endpoint::store::StoreCreatedEvent event_cast = store::Events::extractStoreCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::store2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreUpdatedEvent(event)) {
            privmx::endpoint::store::StoreUpdatedEvent event_cast = store::Events::extractStoreUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::store2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreStatsChangedEvent(event)) {
            privmx::endpoint::store::StoreStatsChangedEvent event_cast = store::Events::extractStoreStatsChangedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::storeStatsChangedEventData2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreUpdatedEvent(event)) {
            privmx::endpoint::store::StoreUpdatedEvent event_cast = store::Events::extractStoreUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::store2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreDeletedEvent(event)) {
            privmx::endpoint::store::StoreDeletedEvent event_cast = store::Events::extractStoreDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::storeDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreFileCreatedEvent(event)) {
            privmx::endpoint::store::StoreFileCreatedEvent event_cast = store::Events::extractStoreFileCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::file2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreFileUpdatedEvent(event)) {
            privmx::endpoint::store::StoreFileUpdatedEvent event_cast = store::Events::extractStoreFileUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::storeFileUpdatedEventData2Java(ctx, event_cast.data)
            );
        } else if (store::Events::isStoreFileDeletedEvent(event)) {
            privmx::endpoint::store::StoreFileDeletedEvent event_cast = store::Events::extractStoreFileDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::storeFileDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (inbox::Events::isInboxCreatedEvent(event)) {
            privmx::endpoint::inbox::InboxCreatedEvent event_cast = inbox::Events::extractInboxCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::inbox2Java(ctx, event_cast.data)
            );
        } else if (inbox::Events::isInboxUpdatedEvent(event)) {
            privmx::endpoint::inbox::InboxUpdatedEvent event_cast = inbox::Events::extractInboxUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::inbox2Java(ctx, event_cast.data)
            );
        } else if (inbox::Events::isInboxDeletedEvent(event)) {
            privmx::endpoint::inbox::InboxDeletedEvent event_cast = inbox::Events::extractInboxDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::inboxDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (inbox::Events::isInboxEntryCreatedEvent(event)) {
            privmx::endpoint::inbox::InboxEntryCreatedEvent event_cast = inbox::Events::extractInboxEntryCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::inboxEntry2Java(ctx, event_cast.data)
            );
        } else if (inbox::Events::isInboxEntryDeletedEvent(event)) {
            privmx::endpoint::inbox::InboxEntryDeletedEvent event_cast = inbox::Events::extractInboxEntryDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::inboxEntryDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbCreatedEvent(event)) {
            privmx::endpoint::kvdb::KvdbCreatedEvent event_cast = kvdb::Events::extractKvdbCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdb2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbDeletedEvent(event)) {
            privmx::endpoint::kvdb::KvdbDeletedEvent event_cast = kvdb::Events::extractKvdbDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdbDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbUpdatedEvent(event)) {
            privmx::endpoint::kvdb::KvdbUpdatedEvent event_cast = kvdb::Events::extractKvdbUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdb2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbStatsEvent(event)) {
            privmx::endpoint::kvdb::KvdbStatsChangedEvent event_cast = kvdb::Events::extractKvdbStatsEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdbStatsEventData2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbNewEntryEvent(event)) {
            privmx::endpoint::kvdb::KvdbNewEntryEvent event_cast = kvdb::Events::extractKvdbNewEntryEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdbEntry2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbEntryUpdatedEvent(event)) {
            privmx::endpoint::kvdb::KvdbEntryUpdatedEvent event_cast = kvdb::Events::extractKvdbEntryUpdatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdbEntry2Java(ctx, event_cast.data)
            );
        } else if (kvdb::Events::isKvdbEntryDeletedEvent(event)) {
            privmx::endpoint::kvdb::KvdbEntryDeletedEvent event_cast = kvdb::Events::extractKvdbEntryDeletedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::kvdbDeletedEntryEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamRoomCreatedEvent(event)) {
            privmx::endpoint::stream::StreamRoomCreatedEvent event_cast = stream::Events::extractStreamRoomCreatedEvent(
                    event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamRoom2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamRoomUpdatedEvent(event)) {
            privmx::endpoint::stream::StreamRoomUpdatedEvent event_cast =
                    stream::Events::extractStreamRoomUpdatedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamRoom2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamRoomDeletedEvent(event)) {
            privmx::endpoint::stream::StreamRoomDeletedEvent event_cast =
                    stream::Events::extractStreamRoomDeletedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamRoomDeletedEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamPublishedEvent(event)) {
            privmx::endpoint::stream::StreamPublishedEvent event_cast =
                    stream::Events::extractStreamPublishedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamPublishedEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamUpdatedEvent(event)) {
            privmx::endpoint::stream::StreamUpdatedEvent event_cast =
                    stream::Events::extractStreamUpdatedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamUpdatedEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamJoinedEvent(event)) {
            privmx::endpoint::stream::StreamJoinedEvent event_cast =
                    stream::Events::extractStreamJoinedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamUnpublishedEvent(event)) {
            privmx::endpoint::stream::StreamUnpublishedEvent event_cast =
                    stream::Events::extractStreamUnpublishedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamUnpublishedEventData2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamLeftEvent(event)) {
            privmx::endpoint::stream::StreamLeftEvent event_cast =
                    stream::Events::extractStreamLeftEvent(event);
            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamLeftEventData2Java(ctx, event_cast.data)
            );
        }
        else if (stream::Events::isRemoteStreamsChangedEvent(event)) {
            privmx::endpoint::stream::RemoteStreamsChangedEvent event_cast =
                    stream::Events::extractRemoteStreamsChangedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::newStreams2Java(ctx, event_cast.data)
            );
        } else if (stream::Events::isStreamsUpdatedEvent(event)) {
            privmx::endpoint::stream::StreamsUpdatedEvent event_cast =
                    stream::Events::extractStreamsUpdatedEvent(event);

            return initEvent(
                    ctx,
                    event_cast.type,
                    event_cast.channel,
                    event_cast.connectionId,
                    event_cast.subscriptions,
                    event_cast.timestamp,
                    privmx::wrapper::streamsUpdated2Java(ctx, event_cast.data)
            );
        } else {
            return initEvent(
                    ctx,
                    event->type,
                    event->channel,
                    event->connectionId,
                    event->subscriptions,
                    event->timestamp,
                    ctx.getKotlinUnit()
            );
        }
    } catch (const std::exception &e) {
        throw e;
    }
    return nullptr;
}

privmx::endpoint::core::PagingQuery parsePagingQuery(
        JniContextUtils &ctx,
        jobject pagingQuery
) {
    auto result = privmx::endpoint::core::PagingQuery();
    if (pagingQuery == nullptr) return result;
    jclass queryClass = ctx->GetObjectClass(pagingQuery);
    jfieldID skipFID = ctx->GetFieldID(queryClass, "skip", "Ljava/lang/Long;");
    jfieldID limitFID = ctx->GetFieldID(queryClass, "limit", "Ljava/lang/Long;");
    jfieldID sortOrderFID = ctx->GetFieldID(queryClass, "sortOrder", "Ljava/lang/String;");
    jfieldID lastIdFID = ctx->GetFieldID(queryClass, "lastId", "Ljava/lang/String;");
    jfieldID queryAsJsonFID = ctx->GetFieldID(queryClass, "queryAsJson", "Ljava/lang/String;");
    jfieldID sortByFID = ctx->GetFieldID(queryClass, "sortBy", "Ljava/lang/String;");

    result.skip = ctx.getObject(ctx->GetObjectField(pagingQuery, skipFID)).getLongValue();
    result.limit = ctx.getObject(ctx->GetObjectField(pagingQuery, limitFID)).getLongValue();
    result.sortOrder = ctx.jString2string((jstring) ctx->GetObjectField(pagingQuery, sortOrderFID));

    jstring value;
    if ((value = (jstring) ctx->GetObjectField(pagingQuery, lastIdFID)) != NULL) {
        result.lastId = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(pagingQuery, queryAsJsonFID)) != NULL) {
        result.queryAsJson = ctx.jString2string(value);
    }
    if ((value = (jstring) ctx->GetObjectField(pagingQuery, sortByFID)) != NULL) {
        result.sortBy = ctx.jString2string(value);
    }

    return result;
}


// streams

privmx::endpoint::stream::StreamHandle parseStreamHandle(
        JniContextUtils &ctx,
        jobject streamHandle
) {
    jclass streamHandleCls = ctx->GetObjectClass(streamHandle);
    jfieldID valueFID = ctx->GetFieldID(streamHandleCls, "value", "J");

    return ctx->GetLongField(streamHandle, valueFID);
}

privmx::endpoint::stream::StreamSubscription parseStreamSubscription(JniContextUtils &ctx, jobject streamSubscription) {
    privmx::endpoint::stream::StreamSubscription result;
    jclass cls = ctx->GetObjectClass(streamSubscription);
    jfieldID streamIdFID = ctx->GetFieldID(
            cls,
            "streamId",
            "Ljava/lang/Long;"
    );

    jfieldID trackIdFID = ctx->GetFieldID(
            cls,
            "streamTrackId",
            "Ljava/lang/String;"
    );

    jobject streamId = ctx->GetObjectField(streamSubscription, streamIdFID);
    jobject streamTrackId = ctx->GetObjectField(streamSubscription, trackIdFID);

    result.streamId = jobject2long(ctx, streamId);
    if (streamTrackId != nullptr) result.streamTrackId = jobject2string(ctx, streamTrackId);

    return result;
}

privmx::endpoint::stream::SdpWithTypeModel parseSdpWithTypeModel(JniContextUtils &ctx, jobject sdpWithTypeModel) {
    jclass cls = ctx->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/stream/SdpWithTypeModel");

    jfieldID sdpFID = ctx->GetFieldID(cls, "sdp", "Ljava/lang/String;");
    jfieldID typeFID = ctx->GetFieldID(cls, "type", "Ljava/lang/String;");

    jstring sdp_j = (jstring) ctx->GetObjectField(sdpWithTypeModel, sdpFID);
    jstring type_j = (jstring) ctx->GetObjectField(sdpWithTypeModel, typeFID);

    privmx::endpoint::stream::SdpWithTypeModel result;
    result.sdp = ctx.jString2string(sdp_j);
    result.type = ctx.jString2string(type_j);

    return result;
}


privmx::endpoint::stream::StreamEncryptionMode parseStreamEncryptionMode(
        JniContextUtils &ctx,
        jobject streamEncryptionMode
) {
    jclass cls = ctx->GetObjectClass(streamEncryptionMode);
    jmethodID nameFID = ctx->GetMethodID(
            cls,
            "name",
            "()Ljava/lang/String;"
    );

    auto name_j = (jstring) ctx->CallObjectMethod(streamEncryptionMode, nameFID);
    std::string name_c = ctx.jString2string(name_j);

    if (name_c == "SINGLE_KEY") return privmx::endpoint::stream::StreamEncryptionMode::SINGLE_KEY;
    if (name_c == "MULTIPLE_KEY") return privmx::endpoint::stream::StreamEncryptionMode::MULTIPLE_KEY;

    return {};
}

int64_t jobject2long(JniContextUtils &ctx, jobject jLong) {
    jclass longClass = ctx->FindClass("java/lang/Long");
    jmethodID longValueMethod = ctx->GetMethodID(longClass, "longValue", "()J");
    jlong value = ctx->CallLongMethod(jLong, longValueMethod);
    return (int64_t) value;
}

std::string jobject2string(JniContextUtils &ctx, jobject jString) {
    auto js = (jstring) jString;
    return ctx.jString2string(js);
}


// c++ -> java
//template<typename T, typename F>
//jobject vectorTojArray(
//        JniContextUtils &ctx,
//        const std::vector<T> &vector,
//        F fun
//) {
//    jclass arrayListCls = ctx->FindClass("java/util/ArrayList");
//    jmethodID initMID = ctx->GetMethodID(arrayListCls, "<init>", "()V");
//    jmethodID addToListMID = ctx->GetMethodID(arrayListCls, "add", "(Ljava/lang/Object;)Z");
//
//    jobject listObj = ctx->NewObject(arrayListCls, initMID);
//
//    for (const auto &item: vector) {
//        jobject jItem = fun(ctx, item);
//        ctx->CallBooleanMethod(listObj, addToListMID, jItem);
//    }
//
//    return listObj;
//}


jobject string2jobject(JniContextUtils &ctx, const std::string &str) {
    return ctx->NewStringUTF(str.c_str());
}

jobject long2jobject(JniContextUtils &ctx, const int64_t &lng) {
    return ctx.long2jLong(lng);
}