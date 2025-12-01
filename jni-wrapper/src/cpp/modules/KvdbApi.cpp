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
#include <privmx/endpoint/kvdb/KvdbApi.hpp>
#include <jni.h>
#include "../model_native_initializers.h"

using namespace privmx::endpoint;

kvdb::KvdbApi *getKvdbApi(JniContextUtils &ctx, jobject kvdbApiInstance) {
    jclass cls = ctx->GetObjectClass(kvdbApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(kvdbApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("KvdbApi cannot be used");
    }
    return (kvdb::KvdbApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &env, &connection] {
        auto connection_c = getConnection(env, connection);
        auto kvdbApi = kvdb::KvdbApi::create(*connection_c);
        auto kvdbApi_ptr = new kvdb::KvdbApi();
        *kvdbApi_ptr = kvdbApi;

        return ctx.long2jLong((jlong) kvdbApi_ptr);
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        auto api = getKvdbApi(ctx, thiz);
        delete api;
        jclass cls = env->GetObjectClass(thiz);
        jfieldID apiFID = env->GetFieldID(cls, "api", "Ljava/lang/Long;");
        env->SetObjectField(thiz, apiFID, (jobject) nullptr);
    } catch (const IllegalStateException &e) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), e.what());
    }
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_createKvdb(
        JNIEnv *env, jobject thiz,
        jstring context_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jobject policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") || ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") || ctx.nullCheck(public_meta, "Public meta") ||
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
                    &policies]() {
                auto container_policies_n = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, policies));

                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx, ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx, ctx.jObject2jArray(managers));

                return ctx->NewStringUTF(
                        getKvdbApi(ctx, thiz)->createKvdb(
                                ctx.jString2string(context_id), users_c,
                                managers_c, core::Buffer::from(
                                        ctx.jByteArray2String(
                                                public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(
                                        private_meta)),
                                container_policies_n).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_updateKvdb(
        JNIEnv *env, jobject thiz,
        jstring kvdb_id,
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
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }

    ctx.callVoidEndpointApi([
                                    &ctx,
                                    &thiz,
                                    &kvdb_id,
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

        getKvdbApi(ctx, thiz)->updateKvdb(
                ctx.jString2string(kvdb_id),
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_deleteKvdb(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &kvdb_id]() {
        getKvdbApi(ctx, thiz)->deleteKvdb(
                ctx.jString2string(kvdb_id)
        );
    });
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_getKvdb(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &kvdb_id]() {
                return privmx::wrapper::kvdb2Java(
                        ctx,
                        getKvdbApi(ctx, thiz)->getKvdb(
                                ctx.jString2string(kvdb_id))
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_listKvdbs(
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto kvdbs_c(
                        getKvdbApi(ctx, thiz)->listKvdbs(
                                ctx.jString2string(context_id),
                                query
                        )
                );

                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &kvdb_c: kvdbs_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           privmx::wrapper::kvdb2Java(ctx, kvdb_c)
                    );
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(kvdbs_c.totalAvailable),
                        array
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_getEntry(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jstring key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(key, "Key")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &kvdb_id, &key]() {
                auto entry_c(getKvdbApi(ctx, thiz)->getEntry(ctx.jString2string(kvdb_id),
                                                             ctx.jString2string(key)));
                return privmx::wrapper::kvdbEntry2Java(ctx, entry_c);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_hasEntry(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jstring key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(key, "Key")) {
        return JNI_FALSE;
    }
    jboolean result;

    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &thiz, &kvdb_id, &key]() -> jboolean {
                bool check_c = getKvdbApi(ctx, thiz)->hasEntry(
                        ctx.jString2string(kvdb_id),
                        ctx.jString2string(key)
                );

                return (check_c == JNI_TRUE);
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_listEntriesKeys(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &kvdb_id, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto entryKeys_c(
                        getKvdbApi(ctx, thiz)->listEntriesKeys(
                                ctx.jString2string(kvdb_id),
                                query
                        )
                );

                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &entryKey_c: entryKeys_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           ctx->NewStringUTF(entryKey_c.c_str())
                    );
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(entryKeys_c.totalAvailable),
                        array
                );
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_listEntries(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &kvdb_id, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }

                auto entries_c(
                        getKvdbApi(ctx, thiz)->listEntries(
                                ctx.jString2string(kvdb_id),
                                query
                        )
                );

                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &entry_c: entries_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           privmx::wrapper::kvdbEntry2Java(ctx, entry_c)
                    );
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(entries_c.totalAvailable),
                        array
                );
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_setEntry(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id, jstring key,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jbyteArray data,
        jlong version
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(key, "Key") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta") ||
        ctx.nullCheck(data, "Data")) {
        return;
    }

    ctx.callVoidEndpointApi(
            [
                    &ctx,
                    &thiz,
                    &kvdb_id,
                    &key,
                    &public_meta,
                    &private_meta,
                    &data,
                    &version]() {
                getKvdbApi(ctx, thiz)->setEntry(
                        ctx.jString2string(kvdb_id),
                        ctx.jString2string(key),
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        core::Buffer::from(ctx.jByteArray2String(data)),
                        version
                );
            }
    );
}
extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_deleteEntry(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jstring key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(key, "Key")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &kvdb_id, &key] {
        getKvdbApi(ctx, thiz)->deleteEntry(
                ctx.jString2string(kvdb_id),
                ctx.jString2string(key));
    });
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_deleteEntries(
        JNIEnv *env,
        jobject thiz,
        jstring kvdb_id,
        jobject keys
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "Kvdb ID") ||
        ctx.nullCheck(keys, "Keys")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &kvdb_id, &keys]() -> jobject {
                jclass mapCls = ctx->FindClass("java/util/HashMap");
                jmethodID initMapMID = ctx->GetMethodID(mapCls, "<init>", "()V");
                jmethodID putInMap = ctx->GetMethodID(
                        mapCls,
                        "put",
                        "("
                        "Ljava/lang/Object;"    // String
                        "Ljava/lang/Object;"    // Boolean
                        ")Ljava/lang/Object;"
                );

                auto keys_arr = ctx.jObject2jArray(keys);
                auto keys_c = std::vector<std::string>();

                for (int i = 0; i < ctx->GetArrayLength(keys_arr); i++) {
                    jobject arrayElement = ctx->GetObjectArrayElement(keys_arr, i);

                    if (arrayElement == nullptr) {
                        env->ThrowNew(
                                env->FindClass("java/lang/NullPointerException"),
                                "At least one of the keys has a null value."
                        );
                        return nullptr;
                    }
                    keys_c.push_back(ctx.jString2string((jstring) arrayElement));
                }
                std::map<std::string, bool> statuses_c = getKvdbApi(ctx, thiz)->deleteEntries(
                        ctx.jString2string(kvdb_id),
                        keys_c
                );

                jobject map = ctx->NewObject(mapCls, initMapMID);
                for (auto &status_c: statuses_c) {
                    ctx->CallObjectMethod(
                            map,
                            putInMap,
                            ctx->NewStringUTF(status_c.first.c_str()),
                            ctx.bool2jBoolean(status_c.second == JNI_TRUE)
                    );
                }
                return map;
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_subscribeFor(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_queries
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_queries, "Subscription queries")) {
        return nullptr;
    }

    jobject result = nullptr;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &subscription_queries]() {
                jclass arrayListCls = env->FindClass("java/util/ArrayList");
                jmethodID initMID = env->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID addToListMID = env->GetMethodID(arrayListCls, "add", "(Ljava/lang/Object;)Z");

                auto subscription_queries_arr = ctx.jObject2jArray(subscription_queries);
                auto subscription_queries_c = std::vector<std::string>();

                int length = ctx->GetArrayLength(subscription_queries_arr);
                for (int i = 0; i < length; i++) {
                    jobject arrayElement = ctx->GetObjectArrayElement(subscription_queries_arr, i);
                    subscription_queries_c.push_back(ctx.jString2string((jstring) arrayElement));
                }

                jobject arrayList = env->NewObject(arrayListCls, initMID);
                auto subscription_ids_c = getKvdbApi(ctx, thiz)->subscribeFor(
                        subscription_queries_c);

                for (auto &id_str : subscription_ids_c) {
                    jstring java_id_str = ctx->NewStringUTF(id_str.c_str());
                    env->CallBooleanMethod(arrayList, addToListMID, java_id_str);
                }
                return arrayList;
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_unsubscribeFrom(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_ids
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_ids, "Subscription IDs")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &env, &thiz, &subscription_ids]() {
        auto subscription_ids_arr = ctx.jObject2jArray(subscription_ids);
        auto subscription_ids_c = std::vector<std::string>();

        int length = ctx->GetArrayLength(subscription_ids_arr);
        for (int i = 0; i < length; i++) {
            jobject arrayElement = ctx->GetObjectArrayElement(subscription_ids_arr, i);
        if (ctx.nullCheck(arrayElement, "Subscription ids array elements")) {
            return;
        }
            subscription_ids_c.push_back(ctx.jString2string((jstring) arrayElement));
        }

        getKvdbApi(ctx, thiz)->unsubscribeFrom(subscription_ids_c);
    });
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_buildSubscriptionQuery(
        JNIEnv *env,
        jobject thiz,
        jlong event_type,
        jlong selector_type,
        jstring selector_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(selector_id, "SelectorID")) {
        return nullptr;
    }

    jstring result = nullptr;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz, &event_type, &selector_type, &selector_id]() {
                std::string query_result_c = getKvdbApi(ctx, thiz)->buildSubscriptionQuery(
                        static_cast<kvdb::EventType>(event_type),
                        static_cast<kvdb::EventSelectorType>(selector_type),
                        ctx.jString2string(selector_id)
                );
                return ctx->NewStringUTF(query_result_c.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_kvdb_KvdbApi_buildSubscriptionQueryForSelectedEntry(
        JNIEnv *env, jobject thiz, jlong event_type, jstring kvdb_id, jstring kvdb_entry_key) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(kvdb_id, "KVDB ID") ||
    ctx.nullCheck(kvdb_entry_key, "KVDB Entry key")) {
        return nullptr;
    }

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &thiz, &event_type, &kvdb_id, &kvdb_entry_key]() {
                auto result = getKvdbApi(ctx, thiz)->buildSubscriptionQueryForSelectedEntry(
                        static_cast<kvdb::EventType>(event_type),
                        ctx.jString2string(kvdb_id),
                        ctx.jString2string(kvdb_entry_key)
                );
                return ctx->NewStringUTF(result.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}