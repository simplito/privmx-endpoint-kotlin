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

#include "../utils.hpp"
#include "../parser.h"
#include "Connection.h"
#include "StoreApi.h"
#include "KvdbApi.h"
#include <privmx/endpoint/lock/LockApi.hpp>
#include <jni.h>
#include "../model_native_initializers.h"
#include <privmx/endpoint/search/SearchApi.hpp>

using namespace privmx::endpoint;

search::SearchApi *getSearchApi(JniContextUtils &ctx, jobject searchApiInstance) {
    jclass cls = ctx->GetObjectClass(searchApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(searchApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("SearchApi cannot be used");
    }
    return (search::SearchApi *) ctx.getObject(apiLong).getLongValue();
}

lock::LockApi *getOwnedLockApi(JniContextUtils &ctx, jobject searchApiInstance) {
    jclass cls = ctx->GetObjectClass(searchApiInstance);
    jfieldID lockApiFID = ctx->GetFieldID(cls, "lockApi", "Ljava/lang/Long;");
    jobject lockApiLong = ctx->GetObjectField(searchApiInstance, lockApiFID);
    if (lockApiLong == nullptr) {
        throw IllegalStateException("SearchApi cannot be used");
    }
    return (lock::LockApi *) ctx.getObject(lockApiLong).getLongValue();
}

void setOwnedLockApi(JniContextUtils &ctx, jobject searchApiInstance, lock::LockApi *lockApi) {
    jclass cls = ctx->GetObjectClass(searchApiInstance);
    jfieldID lockApiFID = ctx->GetFieldID(cls, "lockApi", "Ljava/lang/Long;");
    ctx->SetObjectField(searchApiInstance, lockApiFID, ctx.long2jLong((jlong) lockApi));
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection,
        jobject store_api,
        jobject kvdb_api
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &connection, &store_api, &kvdb_api] {
                auto connection_c = getConnection(env, connection);
                auto storeApi_c = getStoreApi(ctx, store_api);
                auto kvdbApi_c = getKvdbApi(ctx, kvdb_api);

                auto lockApi_ptr = new lock::LockApi();
                try {
                    *lockApi_ptr = lock::LockApi::create(*connection_c);
                    auto searchApi = search::SearchApi::create(
                            *connection_c,
                            *storeApi_c,
                            *kvdbApi_c,
                            *lockApi_ptr
                    );
                    auto searchApi_ptr = new search::SearchApi();
                    *searchApi_ptr = searchApi;

                    setOwnedLockApi(ctx, thiz, lockApi_ptr);
                    return ctx.long2jLong((jlong) searchApi_ptr);
                } catch (...) {
                    delete lockApi_ptr;
                    throw;
                }
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        auto api = getSearchApi(ctx, thiz);
        auto lockApi = getOwnedLockApi(ctx, thiz);
        delete api;
        delete lockApi;
        jclass cls = env->GetObjectClass(thiz);
        jfieldID apiFID = env->GetFieldID(cls, "api", "Ljava/lang/Long;");
        env->SetObjectField(thiz, apiFID, (jobject) nullptr);
        jfieldID lockApiFID = env->GetFieldID(cls, "lockApi", "Ljava/lang/Long;");
        env->SetObjectField(thiz, lockApiFID, (jobject) nullptr);
    } catch (const IllegalStateException &e) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), e.what());
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_createSearchIndex(
        JNIEnv *env, jobject thiz,
        jstring context_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong mode,
        jobject policies
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
            [
                    &ctx,
                    &thiz,
                    &context_id,
                    &users,
                    &managers,
                    &public_meta,
                    &private_meta,
                    &mode,
                    &policies]() {
                auto container_policies_n = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, policies));

                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx, ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx, ctx.jObject2jArray(managers));

                return ctx->NewStringUTF(
                        getSearchApi(ctx, thiz)->createSearchIndex(
                                ctx.jString2string(context_id),
                                users_c,
                                managers_c,
                                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                                static_cast<search::IndexMode>(mode),
                                container_policies_n).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_updateSearchIndex(
        JNIEnv *env, jobject thiz,
        jstring index_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong version,
        jboolean force,
        jboolean force_generate_new_key,
        jobject policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(index_id, "Search Index ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }

    ctx.callVoidEndpointApi([
                                    &ctx,
                                    &thiz,
                                    &index_id,
                                    &users,
                                    &managers,
                                    &public_meta,
                                    &private_meta,
                                    &version,
                                    &force,
                                    &force_generate_new_key,
                                    &policies]() {
        auto container_policies_n = std::optional<core::ContainerPolicy>(
                parseContainerPolicy(ctx, policies));

        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx, ctx.jObject2jArray(users));
        std::vector<core::UserWithPubKey> managers_c = usersToVector(
                ctx, ctx.jObject2jArray(managers));

        getSearchApi(ctx, thiz)->updateSearchIndex(
                ctx.jString2string(index_id),
                users_c,
                managers_c,
                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                version,
                force == JNI_TRUE,
                force_generate_new_key == JNI_TRUE,
                container_policies_n
        );
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_deleteSearchIndex(
        JNIEnv *env,
        jobject thiz,
        jstring index_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(index_id, "Search Index ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &index_id]() {
        getSearchApi(ctx, thiz)->deleteSearchIndex(
                ctx.jString2string(index_id)
        );
    });
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_getSearchIndex(
        JNIEnv *env,
        jobject thiz,
        jstring index_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(index_id, "Search Index ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &index_id]() {
                return privmx::wrapper::searchIndex2Java(
                        ctx,
                        getSearchApi(ctx, thiz)->getSearchIndex(
                                ctx.jString2string(index_id))
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_listSearchIndexes(
        JNIEnv *env, jobject thiz,
        jstring context_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &context_id, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto searchIndexes_c(
                        getSearchApi(ctx, thiz)->listSearchIndexes(
                                ctx.jString2string(context_id),
                                query
                        )
                );

                return pagingList2Java(
                        ctx,
                        searchIndexes_c,
                        privmx::wrapper::searchIndex2Java
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_openSearchIndex(
        JNIEnv *env,
        jobject thiz,
        jstring index_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(index_id, "Search Index ID")) {
        return {};
    }

    jlong result;
    ctx.callResultEndpointApi<jlong>(&result, [&ctx, &thiz, &index_id] {
        return (jlong) getSearchApi(ctx, thiz)->openSearchIndex(
                ctx.jString2string(index_id)
        );
    });
    if (ctx->ExceptionCheck()) {
        return {};
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_closeSearchIndex(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle]() {
        getSearchApi(ctx, thiz)->closeSearchIndex(index_handle);
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_beginTransaction(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle]() {
        getSearchApi(ctx, thiz)->beginTransaction(index_handle);
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_commit(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle]() {
        getSearchApi(ctx, thiz)->commit(index_handle);
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_rollback(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle]() {
        getSearchApi(ctx, thiz)->rollback(index_handle);
    });
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_addDocument(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle,
        jstring name,
        jstring content
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(name, "Name") ||
        ctx.nullCheck(content, "Content")) {
        return {};
    }

    jlong result;
    ctx.callResultEndpointApi<jlong>(&result, [&ctx, &thiz, &index_handle, &name, &content] {
        return (jlong) getSearchApi(ctx, thiz)->addDocument(
                index_handle,
                ctx.jString2string(name),
                ctx.jString2string(content)
        );
    });
    if (ctx->ExceptionCheck()) {
        return {};
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_updateDocument(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle,
        jobject document
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(document, "Document")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle, &document]() {
        getSearchApi(ctx, thiz)->updateDocument(
                index_handle,
                parseDocument(ctx, document)
        );
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_deleteDocument(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle,
        jlong document_id
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &index_handle, &document_id]() {
        getSearchApi(ctx, thiz)->deleteDocument(index_handle, document_id);
    });
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_getDocument(
        JNIEnv *env,
        jobject thiz,
        jlong index_handle,
        jlong document_id
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &index_handle, &document_id]() {
                return privmx::wrapper::document2Java(
                        ctx,
                        getSearchApi(ctx, thiz)->getDocument(index_handle, document_id)
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_listDocuments(
        JNIEnv *env, jobject thiz,
        jlong index_handle,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &index_handle, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto documents_c(
                        getSearchApi(ctx, thiz)->listDocuments(
                                index_handle,
                                query
                        )
                );

                return pagingList2Java(
                        ctx,
                        documents_c,
                        privmx::wrapper::document2Java
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_search_SearchApi_searchDocuments(
        JNIEnv *env, jobject thiz,
        jlong index_handle,
        jstring search_query,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(search_query, "Search query") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &index_handle, &search_query, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto documents_c(
                        getSearchApi(ctx, thiz)->searchDocuments(
                                index_handle,
                                ctx.jString2string(search_query),
                                query
                        )
                );

                return pagingList2Java(
                        ctx,
                        documents_c,
                        privmx::wrapper::document2Java
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
