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
#include "LockApi.h"
#include <privmx/endpoint/lock/LockApi.hpp>
#include <jni.h>
#include "../model_native_initializers.h"

using namespace privmx::endpoint;

lock::LockApi *getLockApi(JniContextUtils &ctx, jobject lockApiInstance) {
    jclass cls = ctx->GetObjectClass(lockApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(lockApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("LockApi cannot be used");
    }
    return (lock::LockApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_lock_LockApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &env, &connection] {
        auto connection_c = getConnection(env, connection);
        auto lockApi = lock::LockApi::create(*connection_c);
        auto lockApi_ptr = new lock::LockApi();
        *lockApi_ptr = lockApi;

        return ctx.long2jLong((jlong) lockApi_ptr);
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_lock_LockApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        auto api = getLockApi(ctx, thiz);
        delete api;
        jclass cls = env->GetObjectClass(thiz);
        jfieldID apiFID = env->GetFieldID(cls, "api", "Ljava/lang/Long;");
        env->SetObjectField(thiz, apiFID, (jobject) nullptr);
    } catch (const IllegalStateException &e) {
        env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), e.what());
    }
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_lock_LockApi_lock(
        JNIEnv *env,
        jobject thiz,
        jstring resource_id,
        jstring uuid,
        jlong lock_level
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(resource_id, "Resource ID") ||
        ctx.nullCheck(uuid, "UUID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &resource_id, &uuid, &lock_level]() {
                auto lockOperationResult_c = getLockApi(ctx, thiz)->lock(
                        ctx.jString2string(resource_id),
                        ctx.jString2string(uuid),
                        static_cast<lock::LockLevel>(lock_level)
                );
                return privmx::wrapper::lockOperationResult2Java(ctx, lockOperationResult_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_lock_LockApi_unlock(
        JNIEnv *env,
        jobject thiz,
        jstring resource_id,
        jstring uuid,
        jlong lock_level
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(resource_id, "Resource ID") ||
        ctx.nullCheck(uuid, "UUID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &resource_id, &uuid, &lock_level]() {
                auto lockOperationResult_c = getLockApi(ctx, thiz)->unlock(
                        ctx.jString2string(resource_id),
                        ctx.jString2string(uuid),
                        static_cast<lock::LockLevel>(lock_level)
                );
                return privmx::wrapper::lockOperationResult2Java(ctx, lockOperationResult_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_lock_LockApi_checkReservedLock(
        JNIEnv *env,
        jobject thiz,
        jstring resource_id,
        jstring uuid
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(resource_id, "Resource ID") ||
        ctx.nullCheck(uuid, "UUID")) {
        return JNI_FALSE;
    }

    jboolean result;
    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &thiz, &resource_id, &uuid]() -> jboolean {
                bool check_c = getLockApi(ctx, thiz)->checkReservedLock(
                        ctx.jString2string(resource_id),
                        ctx.jString2string(uuid)
                );

                return (check_c == JNI_TRUE);
            });
    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}
