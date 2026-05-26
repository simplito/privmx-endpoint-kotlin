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

#include <jni.h>
#include <privmx/endpoint/core/Connection.hpp>
#include "privmx/endpoint/core/Config.hpp"
#include <privmx/endpoint/core/Exception.hpp>
#include "UserVerifierInterfaceJNI.h"
#include "Connection.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"
#include <privmx/drv/net.h>

using namespace privmx::endpoint;

privmx::endpoint::core::Connection *getConnection(JNIEnv *env, jobject thiz) {
    JniContextUtils ctx(env);
    jclass cls = ctx->GetObjectClass(thiz);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(thiz, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("Platform is not connected. Connect to platform first.");
    }
    return (privmx::endpoint::core::Connection *) ctx.getObject(apiLong).getLongValue();
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_deinit(JNIEnv *env, jobject thiz) {
    try {
        //if null go to catch
        auto api = getConnection(env, thiz);
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

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_listContexts(
        JNIEnv *env,
        jobject thiz,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring query_as_json,
        jstring sort_by
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sort_order, "Sort Order")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
                auto query = privmx::endpoint::core::PagingQuery();
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
                privmx::endpoint::core::PagingList<privmx::endpoint::core::Context> infos = getConnection(
                        env, thiz)->listContexts(query);
                jclass pagingListCls = env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/PagingList");
                jmethodID pagingListInitMID = env->GetMethodID(
                        pagingListCls,
                        "<init>",
                        "(Ljava/lang/Long;Ljava/util/List;)V");
                jclass arrayListCls = env->FindClass("java/util/ArrayList");
                jmethodID initMID = env->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID addToListMID = env->GetMethodID(arrayListCls,
                                                          "add",
                                                          "(Ljava/lang/Object;)Z");
                jobject array = env->NewObject(arrayListCls, initMID);
                for (auto &context: infos.readItems) {
                    env->CallBooleanMethod(
                            array,
                            addToListMID,
                            privmx::wrapper::context2Java(ctx, context));
                }
                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(infos.totalAvailable),
                        array);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_disconnect(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&env, &thiz]() {
        getConnection(env, thiz)->disconnect();
    });
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_setCertsPath(
        JNIEnv *env,
        jclass clazz,
        jstring certs_path
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(certs_path, "Certs path")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &certs_path]() {
        privmx::endpoint::core::Config::setCertsPath(ctx.jString2string(certs_path));
        //TODO: Should be called in endpoint
        privmxDrvNet_setConfig(std::string("caCertPath=" + ctx.jString2string(certs_path)).c_str());
    });
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_connect(
        JNIEnv *env,
        jclass clazz,
        jstring user_priv_key,
        jstring solution_id,
        jstring bridge_url,
        jobject pki_verification_options
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(user_priv_key, "User Private Key") ||
        ctx.nullCheck(solution_id, "Solution ID") ||
        ctx.nullCheck(bridge_url, "Bridge URL")) {
        return nullptr;
    }
    jobject result;

    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &clazz, &user_priv_key, &solution_id, &bridge_url, &pki_verification_options]() {
                jmethodID initMID = ctx->GetMethodID(
                        clazz,
                        "<init>",
                        "(Ljava/lang/Long;)V");

                privmx::endpoint::core::Connection connection;
                if (pki_verification_options != nullptr) {
                    connection = privmx::endpoint::core::Connection::connect(
                            ctx.jString2string(user_priv_key),
                            ctx.jString2string(solution_id),
                            ctx.jString2string(bridge_url),
                            parsePKIVerificationOptions(ctx, pki_verification_options));
                } else {
                    connection = privmx::endpoint::core::Connection::connect(
                            ctx.jString2string(user_priv_key),
                            ctx.jString2string(solution_id),
                            ctx.jString2string(bridge_url));
                }

                auto *api = new privmx::endpoint::core::Connection();
                *api = connection;
                jobject result = ctx->NewObject(
                        clazz,
                        initMID,
                        ctx.long2jLong((jlong) api));
                return result;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_connectPublic(
        JNIEnv *env,
        jclass clazz,
        jstring solution_id,
        jstring bridge_url,
        jobject pki_verification_options
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(solution_id, "Solution ID") ||
        ctx.nullCheck(bridge_url, "Bridge URL")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &clazz, &solution_id, &bridge_url, &pki_verification_options]() {
                jmethodID initMID = ctx->GetMethodID(
                        clazz,
                        "<init>",
                        "(Ljava/lang/Long;)V");

                privmx::endpoint::core::Connection connection;
                if (pki_verification_options != nullptr) {
                    connection = privmx::endpoint::core::Connection::connectPublic(
                            ctx.jString2string(solution_id),
                            ctx.jString2string(bridge_url),
                            parsePKIVerificationOptions(ctx, pki_verification_options));
                } else {
                    connection = privmx::endpoint::core::Connection::connectPublic(
                            ctx.jString2string(solution_id),
                            ctx.jString2string(bridge_url));
                }

                auto *api = new privmx::endpoint::core::Connection();
                *api = connection;
                jobject result = ctx->NewObject(
                        clazz,
                        initMID,
                        ctx.long2jLong((jlong) api));
                return result;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_getConnectionId(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result, [&ctx, &env, &thiz]() {
                return ctx.long2jLong((jlong) getConnection(env, thiz)->getConnectionId());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_setUserVerifier(
        JNIEnv *env,
        jobject thiz,
        jobject userVerifierInterface
) {
    JniContextUtils ctx(env);
    auto userVerifier = std::make_shared<privmx::wrapper::UserVerifierInterfaceJNI>(
            env, userVerifierInterface
    );

    ctx.callVoidEndpointApi([&env, &thiz, &userVerifier]() {
        getConnection(env, thiz)->setUserVerifier(userVerifier);
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_listContextUsers(
        JNIEnv *env,
        jobject thiz,
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
            [&ctx, &env, &thiz, &context_id, &skip, &limit, &sort_order, &last_id, &query_as_json, &sort_by]() {
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

                auto users_c = getConnection(env, thiz)->listContextUsers(
                        ctx.jString2string(context_id),
                        query
                );

                jobject array = ctx->NewObject(arrayCls, initArrayMID);
                for (auto &user: users_c.readItems) {
                    ctx->CallBooleanMethod(array,
                                           addToArrayMID,
                                           privmx::wrapper::userInfo2Java(ctx, user)
                    );
                }

                return ctx->NewObject(
                        pagingListCls,
                        pagingListInitMID,
                        ctx.long2jLong(users_c.totalAvailable),
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_subscribeFor(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_queries
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_queries, "Subscription queries")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &subscription_queries]() -> jobject {
                jclass arrayListCls = env->FindClass("java/util/ArrayList");
                jmethodID initMID = env->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID addToListMID = env->GetMethodID(arrayListCls, "add",
                                                          "(Ljava/lang/Object;)Z");

                auto subscription_queries_arr = ctx.jObject2jArray(subscription_queries);
                auto subscription_queries_c = std::vector<std::string>();

                for (int i = 0; i < ctx->GetArrayLength(subscription_queries_arr); i++) {
                    jobject arrayElement = ctx->GetObjectArrayElement(subscription_queries_arr, i);
                    if (ctx.nullCheck(arrayElement, "Subscription queries array elements")) {
                        return nullptr;
                    }
                    subscription_queries_c.push_back(ctx.jString2string((jstring) arrayElement));
                }

                auto subscription_ids_c = getConnection(env, thiz)->subscribeFor(
                        subscription_queries_c);

                jobject array = env->NewObject(arrayListCls, initMID);
                for (auto &id: subscription_ids_c) {
                    ctx->CallBooleanMethod(
                            array,
                            addToListMID,
                            ctx->NewStringUTF(id.c_str())
                    );
                }
                return array;
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_unsubscribeFrom(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_ids
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_ids, "Subscription IDs")) {
        return;
    }

    ctx.callVoidEndpointApi(
            [&ctx, &env, &thiz, &subscription_ids]() {
                auto subscription_ids_arr = ctx.jObject2jArray(subscription_ids);
                auto subscription_ids_c = std::vector<std::string>();

                for (int i = 0; i < ctx->GetArrayLength(subscription_ids_arr); i++) {
                    jobject arrayElement = ctx->GetObjectArrayElement(subscription_ids_arr, i);
                    if (ctx.nullCheck(arrayElement, "Subscription ids array elements")) {
                        return;
                    }
                    subscription_ids_c.push_back(ctx.jString2string((jstring) arrayElement));
                }

                getConnection(env, thiz)->unsubscribeFrom(subscription_ids_c);
            }
    );
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_buildSubscriptionQuery(
        JNIEnv *env,
        jobject thiz,
        jlong event_type,
        jlong selector_type,
        jstring selector_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(selector_id, "Selector ID")) {
        return nullptr;
    }

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz, &event_type, &selector_type, &selector_id]() {
                auto result = getConnection(env, thiz)->buildSubscriptionQuery(
                        static_cast<core::EventType>(event_type),
                        static_cast<core::EventSelectorType>(selector_type),
                        ctx.jString2string(selector_id)
                );
                return ctx->NewStringUTF(result.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}