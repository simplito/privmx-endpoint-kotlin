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
#include <privmx/endpoint/inbox/InboxApi.hpp>
#include <privmx/endpoint/core/Exception.hpp>
#include "Connection.h"
#include "ThreadApi.h"
#include "StoreApi.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../model_native_initializers.h"
#include "../exceptions.h"
#include "privmx/endpoint/core/Exception.hpp"

using namespace privmx::endpoint;

inbox::InboxApi *getInboxApi(JniContextUtils &ctx, jobject inboxApiInstance) {
    jclass cls = ctx->GetObjectClass(inboxApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(inboxApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("InboxApi cannot be used");
    }
    return (inbox::InboxApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_init(
        JNIEnv *env, jobject thiz,
        jobject connection,
        jobject thread_api,
        jobject store_api
) {
    JniContextUtils ctx(env);
    try {
        auto connection_c = getConnection(env, connection);
        auto threadApi_c = getThreadApi(ctx, thread_api);
        auto storeApi_c = getStoreApi(ctx, store_api);
        auto inboxApi = inbox::InboxApi::create(
                *connection_c,
                *threadApi_c,
                *storeApi_c
        );
        auto inboxApi_ptr = new inbox::InboxApi();
        *inboxApi_ptr = inboxApi;
        return ctx.long2jLong((jlong) inboxApi_ptr);
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_deinit(JNIEnv *env, jobject thiz) {
    try {
        JniContextUtils ctx(env);
        auto api = getInboxApi(ctx, thiz);
        delete api;
        jclass cls = env->GetObjectClass(thiz);
        jfieldID apiFID = env->GetFieldID(cls, "api", "Ljava/lang/Long;");
        env->SetObjectField(thiz, apiFID, (jobject) nullptr);
    } catch (const IllegalStateException &e) {
        env->ThrowNew(
                env->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_createInbox(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jobject files_config,
        jobject container_policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return nullptr;
    }

    try {
        std::optional<inbox::FilesConfig> files_config_c = std::nullopt;
        if (files_config != nullptr) {
            files_config_c = parseFilesConfig(ctx, files_config);
        }
        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx,
                ctx.jObject2jArray(users)
        );
        std::vector<core::UserWithPubKey> managers_c = usersToVector(
                ctx,
                ctx.jObject2jArray(managers)
        );
        auto container_policies_n = std::optional<core::ContainerPolicyWithoutItem>(
                parseContainerPolicyWithoutItem(ctx, container_policies));
        return ctx->NewStringUTF(
                getInboxApi(ctx, thiz)->createInbox(
                        ctx.jString2string(context_id),
                        users_c,
                        managers_c,
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        files_config_c,
                        container_policies_n
                ).c_str()
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_updateInbox(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jobject files_config,
        jlong version,
        jboolean force,
        jboolean force_generate_new_key,
        jobject container_policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }

    try {
        std::optional<inbox::FilesConfig> files_config_c = std::nullopt;
        if (files_config != nullptr) {
            files_config_c = parseFilesConfig(ctx, files_config);
        }
        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx,
                ctx.jObject2jArray(users)
        );
        std::vector<core::UserWithPubKey> managers_c = usersToVector(
                ctx,
                ctx.jObject2jArray(managers)
        );
        auto container_policies_n = std::optional<core::ContainerPolicyWithoutItem>(
                parseContainerPolicyWithoutItem(ctx, container_policies));
        getInboxApi(ctx, thiz)->updateInbox(
                ctx.jString2string(inbox_id),
                users_c,
                managers_c,
                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                files_config_c,
                version,
                force == JNI_TRUE,
                force_generate_new_key == JNI_TRUE,
                container_policies_n
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_getInbox(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID")) {
        return nullptr;
    }
    try {
        return privmx::wrapper::inbox2Java(
                ctx,
                getInboxApi(ctx, thiz)->getInbox(
                        ctx.jString2string(inbox_id)
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_listInboxes(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jclass pagingListCls = env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/PagingList");
    jmethodID pagingListInitMID = env->GetMethodID(pagingListCls, "<init>",
                                                   "(Ljava/lang/Long;Ljava/util/List;)V");
    jclass arrayCls = env->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = env->GetMethodID(arrayCls, "<init>", "()V");
    jmethodID addToArrayMID = env->GetMethodID(arrayCls, "add", "(Ljava/lang/Object;)Z");
    auto query = core::PagingQuery();
    query.skip = skip;
    query.limit = limit;
    query.sortOrder = ctx.jString2string(sort_order);
    if (last_id != nullptr) {
        query.lastId = ctx.jString2string(last_id);
    }
    try {
        auto inboxes_c(
                getInboxApi(ctx, thiz)->listInboxes(
                        ctx.jString2string(context_id),
                        query
                )
        );
        jobject array = env->NewObject(arrayCls, initArrayMID);
        for (auto &inbox_c: inboxes_c.readItems) {
            env->CallBooleanMethod(array,
                                   addToArrayMID,
                                   privmx::wrapper::inbox2Java(ctx, inbox_c)
            );
        }
        return ctx->NewObject(
                pagingListCls,
                pagingListInitMID,
                ctx.long2jLong(inboxes_c.totalAvailable),
                array
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_getInboxPublicView(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID")) {
        return nullptr;
    }
    try {
        return privmx::wrapper::inboxPublicView2Java(
                ctx,
                getInboxApi(ctx, thiz)->getInboxPublicView(
                        ctx.jString2string(inbox_id)
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_deleteInbox(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID")) {
        return;
    }
    try {
        getInboxApi(ctx, thiz)->deleteInbox(
                ctx.jString2string(inbox_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_prepareEntry(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id,
        jbyteArray data,
        jobject inbox_file_handles,
        jstring user_priv_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID") ||
        ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(inbox_file_handles, "File handles list")) {
        return nullptr;
    }
    try {
        std::optional<std::string> user_priv_key_c = std::nullopt;
        if (user_priv_key != nullptr) {
            user_priv_key_c = ctx.jString2string(user_priv_key);
        }
        auto files_handles_arr = ctx.jObject2jArray(inbox_file_handles);
        auto file_handles_c = std::vector<int64_t>();
        for (int i = 0; i < ctx->GetArrayLength(files_handles_arr); i++) {
            jobject arrayElement = ctx->GetObjectArrayElement(files_handles_arr, i);
            file_handles_c.push_back(ctx.getObject(arrayElement).getLongValue());
        }
        return ctx.long2jLong(
                getInboxApi(ctx, thiz)->prepareEntry(
                        ctx.jString2string(inbox_id),
                        core::Buffer::from(ctx.jByteArray2String(data)),
                        file_handles_c,
                        user_priv_key_c
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_sendEntry(
        JNIEnv *env,
        jobject thiz,
        jlong inbox_handle
) {
    JniContextUtils ctx(env);
    try {
        getInboxApi(ctx, thiz)->sendEntry(inbox_handle);
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_readEntry(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_entry_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_entry_id, "Inbox Entry ID")) {
        return nullptr;
    }
    try {
        return privmx::wrapper::inboxEntry2Java(
                ctx,
                getInboxApi(ctx, thiz)->readEntry(
                        ctx.jString2string(inbox_entry_id)
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_listEntries(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }
    jclass pagingListCls = env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/PagingList");
    jmethodID pagingListInitMID = env->GetMethodID(pagingListCls, "<init>",
                                                   "(Ljava/lang/Long;Ljava/util/List;)V");
    jclass arrayCls = env->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = env->GetMethodID(arrayCls, "<init>", "()V");
    jmethodID addToArrayMID = env->GetMethodID(arrayCls, "add", "(Ljava/lang/Object;)Z");
    auto query = core::PagingQuery();
    query.skip = skip;
    query.limit = limit;
    query.sortOrder = ctx.jString2string(sort_order);
    if (last_id != nullptr) {
        query.lastId = ctx.jString2string(last_id);
    }
    try {
        auto entries_c(
                getInboxApi(ctx, thiz)->listEntries(
                        ctx.jString2string(inbox_id),
                        query
                )
        );
        jobject array = env->NewObject(arrayCls, initArrayMID);
        for (auto &entry_c: entries_c.readItems) {
            env->CallBooleanMethod(array,
                                   addToArrayMID,
                                   privmx::wrapper::inboxEntry2Java(ctx, entry_c)
            );
        }
        return ctx->NewObject(
                pagingListCls,
                pagingListInitMID,
                ctx.long2jLong(entries_c.totalAvailable),
                array
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_deleteEntry(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_entry_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_entry_id, "Inbox Entry ID")) {
        return;
    }
    try {
        getInboxApi(ctx, thiz)->deleteEntry(ctx.jString2string(inbox_entry_id));
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_createFileHandle(
        JNIEnv *env,
        jobject thiz,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong file_size
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(public_meta, "Public Meta") ||
        ctx.nullCheck(private_meta, "Private Meta")) {
        return nullptr;
    }
    try {
        return ctx.long2jLong(
                getInboxApi(ctx, thiz)->createFileHandle(
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        file_size
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_writeToFile(
        JNIEnv *env,
        jobject thiz,
        jlong inbox_handle,
        jlong inbox_file_handle,
        jbyteArray data_chunk
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data_chunk, "Data chunk")) {
        return;
    }
    try {
        auto data_chunk_c = ctx.jByteArray2String(data_chunk);
        getInboxApi(ctx, thiz)->writeToFile(
                inbox_handle,
                inbox_file_handle,
                core::Buffer::from(data_chunk_c)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_openFile(
        JNIEnv *env,
        jobject thiz,
        jstring file_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID")) {
        return nullptr;
    }
    try {
        return ctx.long2jLong(
                (jlong) getInboxApi(ctx, thiz)->openFile(
                        ctx.jString2string(file_id)
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_readFromFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jlong length
) {
    JniContextUtils ctx(env);
    try {
        auto data_c = getInboxApi(ctx, thiz)->readFromFile(file_handle, length).stdString();
        jbyteArray data = ctx->NewByteArray(data_c.length());
        ctx->SetByteArrayRegion(
                data,
                0,
                data_c.length(),
                (jbyte *) data_c.c_str()
        );
        return data;
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_seekInFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jlong position
) {
    JniContextUtils ctx(env);
    try {
        getInboxApi(ctx, thiz)->seekInFile(
                file_handle,
                position
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_closeFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle
) {
    JniContextUtils ctx(env);
    try {
        return ctx->NewStringUTF(
                getInboxApi(ctx, thiz)->closeFile(file_handle)
                        .c_str()
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_subscribeForInboxEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    try {
        getInboxApi(ctx, thiz)->subscribeForInboxEvents();
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_unsubscribeFromInboxEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    try {
        getInboxApi(ctx, thiz)->unsubscribeFromInboxEvents();
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_subscribeForEntryEvents(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID")) {
        return;
    }
    try {
        getInboxApi(ctx, thiz)->subscribeForEntryEvents(
                ctx.jString2string(inbox_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_inbox_InboxApi_unsubscribeFromEntryEvents(
        JNIEnv *env,
        jobject thiz,
        jstring inbox_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(inbox_id, "Inbox ID")) {
        return;
    }
    try {
        getInboxApi(ctx, thiz)->unsubscribeFromEntryEvents(
                ctx.jString2string(inbox_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}