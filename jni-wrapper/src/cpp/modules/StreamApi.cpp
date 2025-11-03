#include "StreamApi.h"

stream::StreamApi *getStreamApi(JniContextUtils &ctx, jobject streamApiInstance) {
    jclass cls = ctx->GetObjectClass(streamApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(streamApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("StreamApi cannot be used");
    }
    return (stream::StreamApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection,
        jobject event_api
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &connection, &event_api]() {
                auto connection_c = getConnection(env, connection);
                auto event_api_c = getEventApi(ctx, event_api);
                auto streamApi = stream::StreamApi::create(*connection_c, *event_api_c);
                auto streamApi_ptr = new stream::StreamApi();
                *streamApi_ptr = streamApi;
                return ctx.long2jLong((jlong) streamApi_ptr);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_deinit(
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
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_subscribeFor(
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
            [&ctx, &thiz, &subscription_queries]() -> jobject {

                std::vector<std::string> queries = jArrayToVector<std::string>(
                        ctx,
                        ctx.jObject2jArray(subscription_queries),
                        jobject2string
                );

                std::vector<std::string> subscription_ids_c =
                        getStreamApi(ctx, thiz)->subscribeFor(queries);

                return vectorTojArray(
                        ctx,
                        subscription_ids_c,
                        string2jobject
                );
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_buildSubscriptionQuery(
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
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_unsubscribeFrom(
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
                jobject2string
        );

        getStreamApi(ctx, thiz)->unsubscribeFrom(subscription_ids_c);
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_listStreams(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &stream_room_id]() {
                std::vector<privmx::endpoint::stream::Stream> streams_c = getStreamApi(
                        ctx, thiz)->listStreams(
                        ctx.jString2string(stream_room_id)
                );

                return vectorTojArray(ctx, streams_c,   privmx::wrapper::stream2Java);
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_createStream(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &stream_room_id]() {
                auto stream_c(
                        getStreamApi(ctx, thiz)->createStream(
                                ctx.jString2string(stream_room_id)
                        )
                );
                return privmx::wrapper::streamHandle2Java(ctx, stream_c);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_getMediaDevices(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz]() {
                auto media_devices_c(
                        getStreamApi(ctx, thiz)->getMediaDevices()
                );
                return vectorTojArray(ctx, media_devices_c, privmx::wrapper::mediaDevice2Java);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_addTrack(
        JNIEnv *env, jobject thiz,
        jobject stream_handle,
        jobject track
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_handle, "Stream Handle") ||
        ctx.nullCheck(track, "Media Device")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_handle, &track]() {
        getStreamApi(ctx, thiz)->addTrack(
                parseStreamHandle(ctx, stream_handle),
                parseMediaDevice(ctx, track)
        );
    });
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_removeTrack(
        JNIEnv *env,
        jobject thiz,
        jobject stream_handle,
        jobject track
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_handle, "Stream Handle") ||
        ctx.nullCheck(track, "Media Device")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_handle, &track]() {
        getStreamApi(ctx, thiz)->removeTrack(
                parseStreamHandle(ctx, stream_handle),
                parseMediaDevice(ctx, track)
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_publishStream(
        JNIEnv *env,
        jobject thiz,
        jobject stream_handle
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_handle, "Stream Handle")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &stream_handle]() {
                auto id =
                        getStreamApi(ctx, thiz)->publishStream(
                                parseStreamHandle(ctx, stream_handle));
                return privmx::wrapper::remoteStreamId2Java(ctx, id);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_unpublishStream(
        JNIEnv *env,
        jobject thiz,
        jobject stream_handle
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_handle]() {
        getStreamApi(ctx, thiz)->unpublishStream(
                parseStreamHandle(ctx, stream_handle)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_subscribeToRemoteStreams(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject subscriptions,
        jobject options
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID") ||
        ctx.nullCheck(options, "Options") ||
        ctx.nullCheck(subscriptions, "Subscriptions List")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id, &subscriptions, &options, &env]() {
        std::vector<privmx::endpoint::stream::StreamSubscription> subscriptions_c =
                jArrayToVector<privmx::endpoint::stream::StreamSubscription>(
                        ctx,
                        ctx.jObject2jArray(
                                subscriptions),
                        parseStreamSubscription
                );
        getStreamApi(ctx, thiz)->subscribeToRemoteStreams(
                ctx.jString2string(stream_room_id),
                subscriptions_c,
                parseStreamSettings(env, options)
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_modifyRemoteStreamsSubscriptions(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject subscriptions_to_add,
        jobject subscriptions_to_remove,
        jobject options
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID") ||
        ctx.nullCheck(options, "Options") ||
        ctx.nullCheck(subscriptions_to_add, "Subscriptions To Add List") ||
        ctx.nullCheck(subscriptions_to_remove, "Subscriptions To Remove List")) {
        return;
    }

    ctx.callVoidEndpointApi(
            [&ctx, &thiz, &stream_room_id, &subscriptions_to_add, &subscriptions_to_remove, &options, &env]() {
                std::vector<privmx::endpoint::stream::StreamSubscription> subscriptions_to_add_c =
                        jArrayToVector<privmx::endpoint::stream::StreamSubscription>(
                                ctx,
                                ctx.jObject2jArray(
                                        subscriptions_to_add),
                                parseStreamSubscription
                        );
                std::vector<privmx::endpoint::stream::StreamSubscription> subscriptions_to_remove_c =
                        jArrayToVector<privmx::endpoint::stream::StreamSubscription>(
                                ctx,
                                ctx.jObject2jArray(
                                        subscriptions_to_remove),
                                parseStreamSubscription
                        );
                getStreamApi(ctx, thiz)->modifyRemoteStreamsSubscriptions(
                        ctx.jString2string(stream_room_id),
                        subscriptions_to_add_c,
                        subscriptions_to_remove_c,
                        parseStreamSettings(env, options)
                );
            });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_unsubscribeFromRemoteStreams(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jobject subscriptions_to_remove
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID") ||
        ctx.nullCheck(subscriptions_to_remove, "Subscriptions To Remove List")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id, &subscriptions_to_remove]() {
        std::vector<privmx::endpoint::stream::StreamSubscription> subscriptions_to_remove_c =
                jArrayToVector<privmx::endpoint::stream::StreamSubscription>(
                        ctx,
                        ctx.jObject2jArray(
                                subscriptions_to_remove),
                        parseStreamSubscription
                );
        getStreamApi(ctx, thiz)->unsubscribeFromRemoteStreams(
                ctx.jString2string(stream_room_id),
                subscriptions_to_remove_c
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_dropBrokenFrames(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id,
        jboolean enable
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id, &enable]() {
        getStreamApi(ctx, thiz)->dropBrokenFrames(
                ctx.jString2string(stream_room_id),
                enable == JNI_TRUE
        );
    });
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_createStreamRoom(
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
                        getStreamApi(ctx, thiz)->createStreamRoom(
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
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_updateStreamRoom(
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
        jobject container_policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID") ||
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
                    &users, &managers,
                    &public_meta,
                    &private_meta,
                    &version,
                    &force,
                    &force_generate_new_key,
                    &container_policies
            ]() {
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                auto container_policies_n = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, container_policies));

                getStreamApi(ctx, thiz)->updateStreamRoom(
                        ctx.jString2string(stream_room_id),
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

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_listStreamRooms(
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
    if (ctx.nullCheck(sort_order, "Sort order") ||
        ctx.nullCheck(context_id, "Context ID")) {
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
                auto streamRooms_c(
                        getStreamApi(ctx, thiz)->listStreamRooms(
                                ctx.jString2string(context_id),
                                query
                        )
                );

                return pagingList2Java(ctx, streamRooms_c, long2jobject);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_getStreamRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &stream_room_id]() {
                auto stream_c(
                        getStreamApi(ctx, thiz)->getStreamRoom(
                                ctx.jString2string(stream_room_id)
                        )
                );
                return privmx::wrapper::streamRoom2Java(ctx, stream_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_deleteStreamRoom(
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
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_joinRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id]() {
        getStreamApi(ctx, thiz)->joinRoom(
                ctx.jString2string(stream_room_id)
        );
    });
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_java_privmx_1endpoint_modules_stream_StreamApi_leaveRoom(
        JNIEnv *env,
        jobject thiz,
        jstring stream_room_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(stream_room_id, "Stream Room ID")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &stream_room_id]() {
        getStreamApi(ctx, thiz)->leaveRoom(
                ctx.jString2string(stream_room_id)
        );
    });
}