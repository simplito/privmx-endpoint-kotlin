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
#include <jni.h>
#include <privmx/endpoint/event/EventApi.hpp>
#include "../utils.hpp"
#include "Connection.h"
#include "../parser.h"

using namespace privmx::endpoint;

event::EventApi *getEventApi(JniContextUtils &ctx, jobject thiz) {
    jclass cls = ctx->GetObjectClass(thiz);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(thiz, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("EventApi cannot be used");
    }
    return (event::EventApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_init(
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
                auto eventApi = event::EventApi::create(*connection_c);
                auto eventApi_ptr = new event::EventApi();
                *eventApi_ptr = eventApi;
                return ctx.long2jLong((jlong) eventApi_ptr);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        //if null go to catch
        auto api = getEventApi(ctx, thiz);
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
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_emitEvent(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jobject users,
        jstring channel_name,
        jbyteArray event_data
) {
    JniContextUtils ctx(env);

    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(channel_name, "Channel name") ||
        ctx.nullCheck(event_data, "Event data")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &context_id, &users, &channel_name, &event_data]() {
        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx,
                ctx.jObject2jArray(users));

        getEventApi(ctx, thiz)->emitEvent(
                ctx.jString2string(context_id), users_c,
                ctx.jString2string(channel_name),
                core::Buffer::from(ctx.jByteArray2String(event_data))
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_subscribeForCustomEvents(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jstring channel_name
) {
    JniContextUtils ctx(env);

    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(channel_name, "Channel name")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &context_id, &channel_name]() {
        getEventApi(ctx, thiz)->subscribeForCustomEvents(
                ctx.jString2string(context_id),
                ctx.jString2string(channel_name)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_unsubscribeFromCustomEvents(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jstring channel_name
) {
    JniContextUtils ctx(env);

    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(channel_name, "Channel name")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &context_id, &channel_name]() {
        getEventApi(ctx, thiz)->unsubscribeFromCustomEvents(
                ctx.jString2string(context_id),
                ctx.jString2string(channel_name)
        );
    });
}