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

#include "model_native_initializers.h"

namespace privmx {
    namespace wrapper {

        //Core
        jobject
        itemPolicy2Java(
                JniContextUtils &ctx,
                privmx::endpoint::core::ItemPolicy itemPolicy
        ) {
            jclass itemPolicyCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/ItemPolicy");
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
                    "com/simplito/java/privmx_endpoint/model/ContainerPolicyWithoutItem");
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
                    "com/simplito/java/privmx_endpoint/model/ContainerPolicy");
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
                    "Lcom/simplito/java/privmx_endpoint/model/ItemPolicy;" // item
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

        //Context
        jobject context2Java(
                JniContextUtils &ctx,
                privmx::endpoint::core::Context context_c
        ) {
            jclass contextCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/Context");
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

        //Threads
        jobject thread2Java(JniContextUtils &ctx, privmx::endpoint::thread::Thread thread_c) {
            jclass threadCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/Thread");
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
                    "Lcom/simplito/java/privmx_endpoint/model/ContainerPolicy;"
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
                    ctx.long2jLong(thread_c.statusCode)
            );
        }

        //Messages
        jobject serverMessageInfo2Java(JniContextUtils &ctx,
                                       privmx::endpoint::thread::ServerMessageInfo serverMessageInfo_c) {
            jclass messageCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/ServerMessageInfo");
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
                    "com/simplito/java/privmx_endpoint/model/Message");
            jmethodID initMessageMID = ctx->GetMethodID(
                    messageCls,
                    "<init>",
                    "(Lcom/simplito/java/privmx_endpoint/model/ServerMessageInfo;[B[B[BLjava/lang/String;Ljava/lang/Long;)V"
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
                    ctx.long2jLong(message_c.statusCode)
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
                    "com/simplito/java/privmx_endpoint/model/Store");
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
                    "Lcom/simplito/java/privmx_endpoint/model/ContainerPolicy;" //policy
                    "Ljava/lang/Long;"  //filesCount
                    "Ljava/lang/Long;"
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
                    ctx.long2jLong(store_c.statusCode)
            );
        }

        //Inbox
        jobject inbox2Java(JniContextUtils &ctx, privmx::endpoint::inbox::Inbox inbox_c) {
            jclass inboxCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/Inbox");
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
                    "Lcom/simplito/java/privmx_endpoint/model/FilesConfig;" //filesConfig
                    "Lcom/simplito/java/privmx_endpoint/model/ContainerPolicyWithoutItem;" //policy
                    "Ljava/lang/Long;" //statusCode
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
                    ctx.long2jLong(inbox_c.statusCode)
            );
        }

        jobject
        inboxEntry2Java(JniContextUtils &ctx, privmx::endpoint::inbox::InboxEntry inboxEntry_c) {
            jclass inboxEntryCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/InboxEntry");
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
                    ctx.long2jLong(inboxEntry_c.statusCode)
            );
        }

        jobject inboxPublicView2Java(JniContextUtils &ctx,
                                     privmx::endpoint::inbox::InboxPublicView inboxPublicView_c) {
            jclass inboxPublicViewCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/InboxPublicView");
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
                    "com/simplito/java/privmx_endpoint/model/FilesConfig");
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
                    "com/simplito/java/privmx_endpoint/model/ServerFileInfo");
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
                    "com/simplito/java/privmx_endpoint/model/File");
            jmethodID initFileMID = ctx->GetMethodID(
                    fileCls,
                    "<init>",
                    "("
                    "Lcom/simplito/java/privmx_endpoint/model/ServerFileInfo;"
                    "[B"
                    "[B"
                    "Ljava/lang/Long;"
                    "Ljava/lang/String;"
                    "Ljava/lang/Long;"
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
                    ctx.long2jLong(file_c.statusCode)
            );
        }

        //Event
        jobject storeFileDeletedEventData2Java(JniContextUtils &ctx,
                                               privmx::endpoint::store::StoreFileDeletedEventData storeFileDeletedEventData_c) {
            jclass storeFileDeletedEventDataCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/events/StoreFileDeletedEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/StoreStatsChangedEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/ThreadDeletedEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/ThreadDeletedMessageEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/StoreDeletedEventData");
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

        jobject threadStatsEventData2Java(
                JniContextUtils &ctx,
                privmx::endpoint::thread::ThreadStatsEventData threadStatsEventData_c
        ) {
            jclass threadStatsEventDataCls = ctx->FindClass(
                    "com/simplito/java/privmx_endpoint/model/events/ThreadStatsEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/InboxDeletedEventData");
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
                    "com/simplito/java/privmx_endpoint/model/events/InboxEntryDeletedEventData");
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
    } // wrapper
} // privmx