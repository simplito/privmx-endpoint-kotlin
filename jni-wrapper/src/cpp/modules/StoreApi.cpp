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
#include <privmx/endpoint/core/Exception.hpp>
#include "Connection.h"
#include "StoreApi.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"

using namespace privmx::endpoint;

store::StoreApi *getStoreApi(JniContextUtils &ctx, jobject storeApiInstance) {
    jclass cls = ctx->GetObjectClass(storeApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(storeApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("StoreApi cannot be used");
    }
    return (store::StoreApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &connection]() {
                auto connection_c = getConnection(env, connection);
                auto storeApi = store::StoreApi::create(*connection_c);
                auto storeApi_ptr = new store::StoreApi();
                *storeApi_ptr = storeApi;
                return ctx.long2jLong((jlong) storeApi_ptr);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        //if null go to catch
        auto api = getStoreApi(ctx, thiz);
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
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_listStores(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sort_order, "Sort order") ||
        ctx.nullCheck(context_id, "Context ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &context_id, &skip, &limit, &sort_order, &last_id, &query_as_json]() {
                jclass pagingListCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/PagingList");
                jmethodID pagingListInitMID = ctx->GetMethodID(pagingListCls, "<init>",
                                                               "(Ljava/lang/Long;Ljava/util/List;)V"
                );
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(arrayCls, "<init>", "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(arrayCls, "add",
                                                           "(Ljava/lang/Object;)Z");
                auto query = core::PagingQuery();
                query.skip = skip;
                query.limit = limit;
                query.sortOrder = ctx.jString2string(sort_order);
                if (last_id != nullptr) {
                    query.lastId = ctx.jString2string(last_id);
                }
                if (query_as_json != nullptr) {
                    query.queryAsJson = ctx.jString2string(query_as_json);
                }
                auto stores_c(
                        getStoreApi(ctx, thiz)->listStores(
                                ctx.jString2string(context_id),
                                query
                        )
                );
                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &store_c: stores_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           privmx::wrapper::store2Java(ctx, store_c)
                    );
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(stores_c.totalAvailable),
                        array
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_getStore(
        JNIEnv *env,
        jobject thiz,
        jstring store_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &store_id]() {
                auto store_c(
                        getStoreApi(ctx, thiz)->getStore(
                                ctx.jString2string(store_id)
                        )
                );
                return privmx::wrapper::store2Java(ctx, store_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_createStore(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
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
    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &thiz, &context_id, &users, &managers, &public_meta, &private_meta, &container_policies]() {
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                auto container_policies_n = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, container_policies));
                return ctx->NewStringUTF(
                        getStoreApi(ctx, thiz)->createStore(
                                ctx.jString2string(context_id),
                                users_c,
                                managers_c,
                                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                                container_policies_n
                        ).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_getFile(
        JNIEnv *env,
        jobject thiz,
        jstring file_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &file_id]() {
                auto file_c(
                        getStoreApi(ctx, thiz)->getFile(
                                ctx.jString2string(file_id)
                        )
                );
                return privmx::wrapper::file2Java(ctx, file_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_listFiles(
        JNIEnv *env,
        jobject thiz,
        jstring store_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &store_id, &skip, &limit, &sort_order, &last_id, &query_as_json]() {
                jclass pagingListCls = ctx->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/PagingList");
                jmethodID pagingListInitMID = ctx->GetMethodID(pagingListCls, "<init>",
                                                               "(Ljava/lang/Long;Ljava/util/List;)V");
                jclass arrayCls = ctx->FindClass("java/util/ArrayList");
                jmethodID initArrayMID = ctx->GetMethodID(arrayCls, "<init>", "()V");
                jmethodID addToArrayMID = ctx->GetMethodID(arrayCls, "add",
                                                           "(Ljava/lang/Object;)Z");
                auto query = core::PagingQuery();
                query.skip = skip;
                query.limit = limit;
                query.sortOrder = ctx.jString2string(sort_order);
                if (last_id != nullptr) {
                    query.lastId = ctx.jString2string(last_id);
                }
                if (query_as_json != nullptr) {
                    query.queryAsJson = ctx.jString2string(query_as_json);
                }
                auto files_c(
                        getStoreApi(ctx, thiz)->listFiles(
                                ctx.jString2string(store_id),
                                query
                        ));
                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &file_c: files_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           privmx::wrapper::file2Java(ctx, file_c));
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(files_c.totalAvailable),
                        array);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_deleteFile(
        JNIEnv *env,
        jobject thiz,
        jstring file_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &file_id]() {
        return getStoreApi(ctx, thiz)->deleteFile(
                ctx.jString2string(file_id)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_deleteStore(
        JNIEnv *env,
        jobject thiz,
        jstring store_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &store_id]() {
        getStoreApi(ctx, thiz)->deleteStore(
                ctx.jString2string(store_id)
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_createFile(
        JNIEnv *env, jobject thiz,
        jstring store_id,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong size
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &store_id, &public_meta, &private_meta, &size]() {
                return ctx.long2jLong(
                        (jlong) getStoreApi(ctx, thiz)->createFile(
                                ctx.jString2string(store_id),
                                core::Buffer::from(
                                        ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(
                                        ctx.jByteArray2String(private_meta)),
                                size));
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_updateFile(
        JNIEnv *env,
        jobject thiz,
        jstring file_id,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong size
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &file_id, &public_meta, &private_meta, &size]() {
                return ctx.long2jLong(
                        (jlong) getStoreApi(ctx, thiz)->updateFile(
                                ctx.jString2string(file_id),
                                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                                size));
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_updateFileMeta(
        JNIEnv *env,
        jobject thiz,
        jstring file_id,
        jbyteArray public_meta,
        jbyteArray private_meta
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &file_id, &public_meta, &private_meta]() {
        getStoreApi(ctx, thiz)->updateFileMeta(
                ctx.jString2string(file_id),
                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                core::Buffer::from(ctx.jByteArray2String(private_meta))
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_writeToFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jbyteArray data_chunk
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data_chunk, "Data chunk")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &data_chunk, &file_handle]() {
        auto data_chunk_c = ctx.jByteArray2String(data_chunk);
        getStoreApi(ctx, thiz)->writeToFile(
                file_handle,
                core::Buffer::from(data_chunk_c)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_updateStore(
        JNIEnv *env,
        jobject thiz,
        jstring store_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong version,
        jboolean force,
        jboolean force_generate_new_key,
        jobject container_policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }
    ctx.callVoidEndpointApi(
            [
                    &ctx,
                    &thiz,
                    &store_id,
                    &users,
                    &managers,
                    &public_meta,
                    &private_meta,
                    &version,
                    &force,
                    &force_generate_new_key,
                    &container_policies]() {
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                auto container_policies_n = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, container_policies));
                getStoreApi(ctx, thiz)->updateStore(
                        ctx.jString2string(store_id),
                        users_c,
                        managers_c,
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        version,
                        force == JNI_TRUE,
                        force_generate_new_key == JNI_TRUE,
                        container_policies_n);
            });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_openFile(
        JNIEnv *env,
        jobject thiz,
        jstring file_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(file_id, "File ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &thiz, &file_id]() {
        return ctx.long2jLong(
                (jlong) getStoreApi(ctx, thiz)->openFile(
                        ctx.jString2string(file_id)
                )
        );
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_readFromFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jlong length
) {
    JniContextUtils ctx(env);
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(&result, [&ctx, &thiz, &file_handle, &length]() {
        auto data_c = getStoreApi(ctx, thiz)->readFromFile(file_handle, length).stdString();
        jbyteArray data = ctx->NewByteArray(data_c.length());
        ctx->SetByteArrayRegion(
                data,
                0,
                data_c.length(),
                (jbyte *) data_c.c_str()
        );
        return data;
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_seekInFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jlong position
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &file_handle, &position]() {
        getStoreApi(ctx, thiz)->seekInFile(
                file_handle,
                position
        );
    });
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_closeFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle
) {
    JniContextUtils ctx(env);
    jstring result;
    ctx.callResultEndpointApi<jstring>(&result, [&ctx, &thiz, &file_handle]() {
        return ctx->NewStringUTF(
                getStoreApi(ctx, thiz)->closeFile(file_handle)
                        .c_str()
        );
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_subscribeForStoreEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz]() {
        getStoreApi(ctx, thiz)->subscribeForStoreEvents();
    });
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_unsubscribeFromStoreEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz]() {
        getStoreApi(ctx, thiz)->unsubscribeFromStoreEvents();
    });
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_subscribeForFileEvents(
        JNIEnv *env,
        jobject thiz,
        jstring store_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &store_id]() {
        getStoreApi(ctx, thiz)->subscribeForFileEvents(
                ctx.jString2string(store_id)
        );
    });
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_store_StoreApi_unsubscribeFromFileEvents(
        JNIEnv *env,
        jobject thiz,
        jstring store_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(store_id, "Store ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &store_id]() {
        getStoreApi(ctx, thiz)->unsubscribeFromFileEvents(
                ctx.jString2string(store_id)
        );
    });
}