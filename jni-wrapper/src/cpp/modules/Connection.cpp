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
#include <privmx/endpoint/core/Connection.hpp>
#include "privmx/endpoint/core/Config.hpp"
#include <privmx/endpoint/core/Exception.hpp>
#include "UserVerifierInterfaceJNI.h"
#include "Connection.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"

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
        jstring query_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sort_order, "Sort Order")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &skip, &limit, &sort_order, &last_id, &query_as_json]() {
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_Connection_getContextUsers(
        JNIEnv *env,
        jobject thiz,
        jstring context_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &context_id]() {

                jclass arrayListCls = env->FindClass("java/util/ArrayList");
                jmethodID initMID = env->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID addToListMID = env->GetMethodID(arrayListCls, "add",
                                                          "(Ljava/lang/Object;)Z");
                jobject array = env->NewObject(arrayListCls, initMID);


                std::vector<privmx::endpoint::core::UserInfo> users = getConnection(
                        env,
                        thiz
                )->getContextUsers(ctx.jString2string(context_id));

                for (auto &user: users) {
                    env->CallBooleanMethod(
                            array,
                            addToListMID,
                            privmx::wrapper::userInfo2Java(ctx, user)
                    );
                }

                return array;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}