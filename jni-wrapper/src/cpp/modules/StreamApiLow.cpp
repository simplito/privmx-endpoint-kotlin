#include <jni.h>

//
// Created by Dawid Jenczewski on 14/02/2025.
//
#include "../utils.hpp"
#include "../parser.h"
#include "../model_native_initializers.h"
#include "Connection.h"

#include "WebRTCInterfaceJNI.h"
#include "privmx/endpoint/stream/StreamApiLow.hpp"

using namespace privmx::endpoint::stream;
using namespace privmx::endpoint;

StreamApiLow *getStreamApi(JniContextUtils &ctx, jobject streamApiInstance) {
    jclass cls = ctx->GetObjectClass(streamApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(streamApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("StreamApiLow cannot be used");
    }
    return (stream::StreamApiLow *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection
) {
    JniContextUtils ctx(env);
    jobject result;

    if (ctx.nullCheck(connection, "Connection")) {
        return nullptr;
    }

    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &env, &connection] {
        auto connection_c = getConnection(env, connection);
        auto streamApiLow = stream::StreamApiLow::create(
                *connection_c
        );
        auto streamApiLow_ptr = new stream::StreamApiLow();
        *streamApiLow_ptr = streamApiLow;
        return ctx.long2jLong((jlong) streamApiLow_ptr);
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        //if null go to catch
        auto api = getStreamApi(ctx, thiz);
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_createStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jobject users,
        jobject managers,
        jbyteArray public_meta,
        jbyteArray private_meta,
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
                    context_id,
                    &users,
                    &managers,
                    &public_meta,
                    &private_meta,
                    &policies
            ]() {
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                auto container_policies_c = std::optional<core::ContainerPolicyWithoutItem>(
                        parseContainerPolicyWithoutItem(ctx, policies));
                return ctx->NewStringUTF(
                        getStreamApi(ctx, thiz)->createStreamRoom(
                                ctx.jString2string(context_id),
                                users_c,
                                managers_c,
                                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                                container_policies_c
                        ).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_updateStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
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
    if (ctx.nullCheck(stream_room_id, "Stream room ID") ||
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
                    &stream_room_id,
                    &users,
                    &managers,
                    &public_meta,
                    &private_meta,
                    &version,
                    force,
                    &force_generate_new_key,
                    &policies
            ]() {
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                auto container_policies_c = std::optional<core::ContainerPolicyWithoutItem>(
                        parseContainerPolicyWithoutItem(ctx, policies));
                getStreamApi(ctx, thiz)->updateStreamRoom(
                        ctx.jString2string(stream_room_id),
                        users_c,
                        managers_c,
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        version,
                        force == JNI_TRUE,
                        force_generate_new_key == JNI_TRUE,
                        container_policies_c
                );
            }
    );
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_listStreamRooms(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id,
        jstring sort_by,
        jstring query_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [
                    &ctx,
                    &env,
                    &thiz,
                    &context_id,
                    &skip,
                    &limit,
                    &sort_order,
                    &last_id,
                    &sort_by,
                    &query_as_json
            ]() {
                auto query = core::PagingQuery();
                query.skip = skip;
                query.limit = limit;
                query.sortOrder = ctx.jString2string(sort_order);
                if (last_id != nullptr) {
                    query.lastId = ctx.jString2string(last_id);
                }
                if (sort_by != nullptr) {
                    query.sortBy = ctx.jString2string(sort_by);
                }
                if (query_as_json != nullptr) {
                    query.queryAsJson = ctx.jString2string(query_as_json);
                }

                auto streamRooms_c(
                        getStreamApi(ctx, thiz)->listStreamRooms(
                                ctx.jString2string(context_id),
                                query
                        )
                );

                jobject array = pagingList2Java(
                        ctx,
                        streamRooms_c,
                        privmx::wrapper::streamRoom2Java
                );

                return array;
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_getStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &thiz, &stream_room_id] {

        return privmx::wrapper::streamRoom2Java(
                ctx,
                getStreamApi(ctx, thiz)->getStreamRoom(
                        ctx.jString2string(stream_room_id)
                )
        );
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_deleteStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id]() {
        getStreamApi(ctx, thiz)->deleteStreamRoom(
                ctx.jString2string(stream_room_id)
        );
    });
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_createStream(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID"))
        return {};

    jlong result;
    ctx.callResultEndpointApi<jlong>(&result, [&ctx, &thiz, &stream_room_id] {
        return (jlong) getStreamApi(ctx, thiz)->createStream(
                ctx.jString2string(stream_room_id)
        );
    });
    if (ctx->ExceptionCheck()) {
        return {};
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_publishStream(
        JNIEnv *env,
        jobject thiz,
        jlong stream_handle
) {
    JniContextUtils ctx(env);

    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &thiz, &stream_handle] {
        auto result = getStreamApi(ctx, thiz)->publishStream(
                stream_handle
        );

        return privmx::wrapper::streamPublishResult2Java(
                ctx,
                result
        );
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_joinStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject web_rtc
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID") ||
        ctx.nullCheck(web_rtc, "webRtc")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &env, &thiz, &stream_room_id, &web_rtc]() {
        auto webrtc = std::make_shared<WebRTCInterfaceJNI>(env, web_rtc);
        std::vector<int64_t> streams_id_c;

        getStreamApi(ctx, thiz)->joinStreamRoom(
                ctx.jString2string(stream_room_id),
                webrtc
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_listStreams(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &thiz, &env, &stream_room_id] {
        auto stream_infos_c = getStreamApi(ctx, thiz)->listStreams(
                ctx.jString2string(stream_room_id)
        );

        jobject array = vectorTojArray(
                ctx,
                stream_infos_c,
                privmx::wrapper::streamInfo2Java
        );

        return array;
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_removeStream(
        JNIEnv *env,
        jobject thiz,
        jlong stream_handle
) {
    JniContextUtils ctx(env);

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_handle]() {
        getStreamApi(ctx, thiz)->removeStream(
                stream_handle
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_leaveStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id]() {
        getStreamApi(ctx, thiz)->leaveStreamRoom(
                ctx.jString2string(stream_room_id)
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_getTurnCredentials(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &env, &thiz] {
        auto turnCredentialsVector = getStreamApi(ctx, thiz)->getTurnCredentials();
        auto array = vectorTojArray(
                ctx,
                turnCredentialsVector,
                privmx::wrapper::turnCredentials2Java
        );

        return array;
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_subscribeFor(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_queries
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_queries, "Subscription queries")) return nullptr;

    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx, &env, &thiz, &subscription_queries] {
        auto subscription_queries_arr = ctx.jObject2jArray(subscription_queries);
        std::vector<std::string> subscription_queries_c = jArrayToVector<std::string>(
                ctx,
                subscription_queries_arr,
                jobject2string,
                false
        );

        auto subscription_ids_c = getStreamApi(ctx, thiz)->subscribeFor(
                subscription_queries_c
        );

        auto arrayList = vectorTojArray(
                ctx,
                subscription_ids_c,
                string2jobject
        );

        return arrayList;

    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_unsubscribeFrom(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_ids
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_ids, "Subscription IDs")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &subscription_ids]() {
        auto subscription_ids_arr = ctx.jObject2jArray(subscription_ids);
        auto subscription_ids_c = jArrayToVector<std::string>(
                ctx,
                subscription_ids_arr,
                jobject2string,
                false
        );
        getStreamApi(ctx, thiz)->unsubscribeFrom(subscription_ids_c);
    });
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_buildSubscriptionQuery(
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
            [&ctx, &thiz, &event_type, &selector_type, &selector_id]() {
                std::string query_result_c = getStreamApi(ctx, thiz)->buildSubscriptionQuery(
                        static_cast<stream::EventType>(event_type),
                        static_cast<stream::EventSelectorType>(selector_type),
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
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_trickle(
        JNIEnv *env,
        jobject thiz,
        jlong session_id,
        jstring candidate_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(candidate_as_json, "Candidate as JSON")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &session_id, &candidate_as_json]() {
        getStreamApi(ctx, thiz)->trickle(
                session_id,
                ctx.jString2string(candidate_as_json)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_removeSubscriberStream(
        JNIEnv *env,
        jobject thiz,
        jlong subscription_handle
) {
    JniContextUtils ctx(env);

    ctx.callVoidEndpointApi([&ctx, &thiz, &subscription_handle]() {
        getStreamApi(ctx, thiz)->removeSubscriberStream(
                subscription_handle
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_updateSubscriberStream(
        JNIEnv *env,
        jobject thiz,
        jlong subscription_handle,
        jobject subscriptions_to_add,
        jobject subscriptions_to_remove
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscriptions_to_add, "Subscriptions to add") ||
            ctx.nullCheck(subscriptions_to_remove, "Subscriptions to remove")) {
        return;
    }

    ctx.callVoidEndpointApi(
            [&ctx, &thiz, &subscription_handle, &subscriptions_to_add, &subscriptions_to_remove]() {
                auto subscriptions_to_add_arr = ctx.jObject2jArray(subscriptions_to_add);
                auto subscriptions_to_remove_arr = ctx.jObject2jArray(subscriptions_to_remove);

                auto subscriptions_to_add_c = jArrayToVector<StreamSubscription>(
                        ctx,
                        subscriptions_to_add_arr,
                        parseStreamSubscription,
                        false
                );
                auto subscriptions_to_remove_c = jArrayToVector<StreamSubscription>(
                        ctx,
                        subscriptions_to_remove_arr,
                        parseStreamSubscription,
                        false
                );

                getStreamApi(ctx, thiz)->updateSubscriberStream(
                        subscription_handle,
                        subscriptions_to_add_c,
                        subscriptions_to_remove_c
                );
            });
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_createSubscriberStream(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject subscriptions
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID") ||
            ctx.nullCheck(subscriptions, "Subscriptions")) {
        return {};
    }

    jlong result;

    ctx.callResultEndpointApi<jlong>(
            &result, [&ctx, &thiz, &stream_room_id, &subscriptions]() {
                auto subscriptions_arr = ctx.jObject2jArray(subscriptions);
                auto subscriptions_c = jArrayToVector<StreamSubscription>(
                        ctx,
                        subscriptions_arr,
                        parseStreamSubscription,
                        false
                );

                return getStreamApi(ctx, thiz)->createSubscriberStream(
                        ctx.jString2string(stream_room_id),
                        subscriptions_c
                );
            });
    if (ctx->ExceptionCheck()) {
        return {};
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_updateStream(
        JNIEnv *env,
        jobject thiz,
        jlong stream_handle
) {
    JniContextUtils ctx(env);

    jobject result;

    ctx.callResultEndpointApi<jobject>(
            &result, [
                    &ctx,
                    &thiz,
                    &stream_handle
            ] {
                auto stream_result = getStreamApi(ctx, thiz)->updateStream(
                        stream_handle
                );

                return privmx::wrapper::streamPublishResult2Java(
                        ctx,
                        stream_result
                );

            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_acceptOfferOnReconfigure(
        JNIEnv *env,
        jobject thiz,
        jlong session_id,
        jobject sdp
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sdp, "SDP")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &session_id, &sdp]() {
        getStreamApi(ctx, thiz)->acceptOfferOnReconfigure(
                session_id,
                parseSdpWithTypeModel(ctx, sdp)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_setNewOfferOnReconfigure(
        JNIEnv *env,
        jobject thiz,
        jlong session_id,
        jobject sdp
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(sdp, "SDP")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &session_id, &sdp]() {
        getStreamApi(ctx, thiz)->setNewOfferOnReconfigure(
                session_id,
                parseSdpWithTypeModel(ctx, sdp)
        );
    });
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_encryptDataChannelMessage(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject plain_message
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID") ||
            ctx.nullCheck(plain_message, "Plain message")) {
        return nullptr;
    }
    jbyteArray result;

    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &stream_room_id, &plain_message]() {
                auto buff = getStreamApi(ctx, thiz)->encryptDataChannelMessage(
                        ctx.jString2string(stream_room_id),
                        parseDataChannelMessage(
                                ctx,
                                plain_message
                        )
                ).stdString();

                jbyteArray data = ctx->NewByteArray(buff.length());

                ctx->SetByteArrayRegion(
                        data,
                        0,
                        buff.length(),
                        (jbyte *) buff.c_str()
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_registerRemoteDataChannel(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jstring remote_stream_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID") ||
            ctx.nullCheck(remote_stream_id, "Remote stream ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id, &remote_stream_id]() {
        getStreamApi(ctx, thiz)->registerRemoteDataChannel(
                ctx.jString2string(stream_room_id),
                ctx.jString2string(remote_stream_id)
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_stream_StreamApiLow_decryptDataChannelMessage(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jstring remote_stream_id,
        jbyteArray encrypted_data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream room ID") ||
            ctx.nullCheck(remote_stream_id, "Remote stream ID") ||
            ctx.nullCheck(encrypted_data, "Encrypted data")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &stream_room_id, &remote_stream_id, &encrypted_data]() {
                auto message_c = getStreamApi(ctx, thiz)->decryptDataChannelMessage(
                        ctx.jString2string(stream_room_id),
                        ctx.jString2string(remote_stream_id),
                        core::Buffer::from(ctx.jByteArray2String(encrypted_data))
                );

                return privmx::wrapper::decryptedDataChannelMessage2Java(
                        ctx,
                        message_c
                );

            });

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}