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

#include "EventApi.h"

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
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_subscribeFor(
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
                auto subscription_ids_c = getEventApi(ctx, thiz)->
                        subscribeFor(subscription_queries_c);

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
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_unsubscribeFrom(
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

        getEventApi(ctx, thiz)->unsubscribeFrom(subscription_ids_c);
    });
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_event_EventApi_buildSubscriptionQuery(
        JNIEnv *env,
        jobject thiz,
        jstring channel_name,
        jlong selector_type,
        jstring selector_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(channel_name, "ChannelName") ||
        ctx.nullCheck(selector_id, "SelectorID")) {
        return nullptr;
    }

    jstring result = nullptr;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz, &channel_name, &selector_type, &selector_id]() {
                std::string query_result_c = getEventApi(ctx, thiz)->buildSubscriptionQuery(
                        ctx.jString2string(channel_name),
                        static_cast<event::EventSelectorType>(selector_type),
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
