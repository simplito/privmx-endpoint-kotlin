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
#include <privmx/endpoint/group/GroupApi.hpp>
#include <privmx/endpoint/core/Exception.hpp>
#include "Connection.h"
#include "GroupApi.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"

using namespace privmx::endpoint;

group::GroupApi *getGroupApi(JniContextUtils &ctx, jobject groupApiInstance) {
    jclass cls = ctx->GetObjectClass(groupApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(groupApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("GroupApi cannot be used");
    }
    return (group::GroupApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_init(
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
                auto groupApi = group::GroupApi::create(*connection_c);
                auto groupApi_ptr = new group::GroupApi();
                *groupApi_ptr = groupApi;
                return ctx.long2jLong((jlong) groupApi_ptr);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        auto api = getGroupApi(ctx, thiz);
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_createGroup(
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
                std::vector<core::UserWithPubKey> users_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(users));
                std::vector<core::UserWithPubKey> managers_c = usersToVector(
                        ctx,
                        ctx.jObject2jArray(managers));
                auto container_policies_opt = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, container_policies));
                return ctx->NewStringUTF(
                        getGroupApi(ctx, thiz)->createGroup(
                                ctx.jString2string(context_id),
                                users_c,
                                managers_c,
                                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                                container_policies_opt
                        ).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_addGroupMembers(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jobject new_members
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(new_members, "New members list")) {
        return;
    }
    ctx.callVoidEndpointApi([&ctx, &thiz, &group_id, &new_members]() {
        std::vector<group::GroupMemberToAdd> new_members_c = groupMembersToVector(
                ctx,
                ctx.jObject2jArray(new_members));
        getGroupApi(ctx, thiz)->addGroupMembers(
                ctx.jString2string(group_id),
                new_members_c
        );
    });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_removeGroupMembers(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jobject user_ids
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(user_ids, "User IDs")) {
        return;
    }

    auto user_ids_c = jArrayToVector<std::string>(
            ctx,
            ctx.jObject2jArray(user_ids),
            jobject2string,
            true
    );
    if (ctx->ExceptionCheck()) return;

    getGroupApi(ctx, thiz)->removeGroupMembers(
            ctx.jString2string(group_id),
            user_ids_c
    );
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_updateGroup(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jlong version,
        jobject container_policies
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(public_meta, "Public meta") ||
            ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }
    ctx.callVoidEndpointApi(
            [&ctx, &thiz, &group_id, &public_meta, &private_meta, &version, &container_policies]() {
                auto container_policies_opt = std::optional<core::ContainerPolicy>(
                        parseContainerPolicy(ctx, container_policies));

                getGroupApi(ctx, thiz)->updateGroup(
                        ctx.jString2string(group_id),
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        version,
                        container_policies_opt
                );
            });
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_deleteGroup(
        JNIEnv *env,
        jobject thiz,
        jstring group_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &group_id]() {
        getGroupApi(ctx, thiz)->deleteGroup(
                ctx.jString2string(group_id)
        );
    });
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_getGroup(
        JNIEnv *env,
        jobject thiz,
        jstring group_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID")) {
        return nullptr;
    }

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &group_id]() {
                group::Group group_c = getGroupApi(ctx, thiz)->getGroup(
                        ctx.jString2string(group_id)
                );
                return privmx::wrapper::group2Java(ctx, group_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_listGroups(
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

                core::PagingList<group::GroupSummary> groups_c = getGroupApi(ctx, thiz)->listGroups(
                        ctx.jString2string(context_id),
                        query
                );

                return pagingList2Java(
                        ctx,
                        groups_c,
                        privmx::wrapper::groupSummary2Java
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_encrypt(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jbyteArray content
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(content, "Content")) {
        return nullptr;
    }
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &group_id, &content]() {
                auto envelope_c = getGroupApi(ctx, thiz)->encrypt(
                        ctx.jString2string(group_id),
                        core::Buffer::from(ctx.jByteArray2String(content))
                ).stdString();
                jbyteArray envelope = ctx->NewByteArray(envelope_c.size());
                ctx->SetByteArrayRegion(
                        envelope,
                        0,
                        envelope_c.size(),
                        (jbyte *) envelope_c.c_str()
                );
                return envelope;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_encryptAnonymously(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jstring group_pub_key,
        jbyteArray content
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(group_pub_key, "Group public key") ||
            ctx.nullCheck(content, "Content")) {
        return nullptr;
    }
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &group_id, &group_pub_key, &content]() {
                auto envelope_c = getGroupApi(ctx, thiz)->encryptAnonymously(
                        ctx.jString2string(group_id),
                        ctx.jString2string(group_pub_key),
                        core::Buffer::from(ctx.jByteArray2String(content))
                ).stdString();
                jbyteArray envelope = ctx->NewByteArray(envelope_c.size());
                ctx->SetByteArrayRegion(
                        envelope,
                        0,
                        envelope_c.size(),
                        (jbyte *) envelope_c.c_str()
                );
                return envelope;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_decrypt(
        JNIEnv *env,
        jobject thiz,
        jbyteArray envelope
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(envelope, "Envelope")) {
        return nullptr;
    }
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &envelope]() {
                group::DecryptedEnvelope decryptedEnvelope_c = getGroupApi(ctx, thiz)->decrypt(
                        core::Buffer::from(ctx.jByteArray2String(envelope))
                );
                return privmx::wrapper::decryptedEnvelope2Java(ctx, decryptedEnvelope_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_beginFileEncryption(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jlong size
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID")) {
        return 0;
    }
    jlong result;
    ctx.callResultEndpointApi<jlong>(
            &result,
            [&ctx, &thiz, &group_id, &size]() {
                return (jlong) getGroupApi(ctx, thiz)->beginFileEncryption(
                        ctx.jString2string(group_id),
                        size
                );
            });
    if (ctx->ExceptionCheck()) {
        return 0;
    }
    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_beginFileEncryptionAnonymously(
        JNIEnv *env,
        jobject thiz,
        jstring group_id,
        jstring group_pub_key,
        jlong size
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(group_id, "Group ID") ||
            ctx.nullCheck(group_pub_key, "Group public key")) {
        return 0;
    }
    jlong result;
    ctx.callResultEndpointApi<jlong>(
            &result,
            [&ctx, &thiz, &group_id, &group_pub_key, &size]() {
                return (jlong) getGroupApi(ctx, thiz)->beginFileEncryptionAnonymously(
                        ctx.jString2string(group_id),
                        ctx.jString2string(group_pub_key),
                        size
                );
            });
    if (ctx->ExceptionCheck()) {
        return 0;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_encryptFileChunk(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jbyteArray plain_chunk
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(plain_chunk, "Plain chunk")) {
        return nullptr;
    }
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &file_handle, &plain_chunk]() {
                auto data_c = getGroupApi(ctx, thiz)->encryptFileChunk(
                        file_handle,
                        core::Buffer::from(ctx.jByteArray2String(plain_chunk))
                ).stdString();
                jbyteArray data = ctx->NewByteArray(data_c.size());
                ctx->SetByteArrayRegion(
                        data,
                        0,
                        data_c.size(),
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
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_finishFileEncryption(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle
) {
    JniContextUtils ctx(env);
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &file_handle]() {
                auto envelope_c = getGroupApi(ctx, thiz)->finishFileEncryption(
                        file_handle
                ).stdString();
                jbyteArray envelope = ctx->NewByteArray(envelope_c.size());
                ctx->SetByteArrayRegion(
                        envelope,
                        0,
                        envelope_c.size(),
                        (jbyte *) envelope_c.c_str()
                );
                return envelope;
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_beginFileDecryption(
        JNIEnv *env,
        jobject thiz,
        jbyteArray envelope
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(envelope, "Envelope")) {
        return 0;
    }
    jlong result;
    ctx.callResultEndpointApi<jlong>(
            &result,
            [&ctx, &thiz, &envelope]() {
                return (jlong) getGroupApi(ctx, thiz)->beginFileDecryption(
                        core::Buffer::from(ctx.jByteArray2String(envelope))
                );
            });
    if (ctx->ExceptionCheck()) {
        return 0;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_decryptFileChunk(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jbyteArray cipher_chunk
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(cipher_chunk, "Cipher chunk")) {
        return nullptr;
    }
    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &thiz, &file_handle, &cipher_chunk]() {
                auto data_c = getGroupApi(ctx, thiz)->decryptFileChunk(
                        file_handle,
                        core::Buffer::from(ctx.jByteArray2String(cipher_chunk))
                ).stdString();
                jbyteArray data = ctx->NewByteArray(data_c.size());
                ctx->SetByteArrayRegion(
                        data,
                        0,
                        data_c.size(),
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
JNIEXPORT jlong JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_seekInEncryptedFile(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle,
        jlong position
) {
    JniContextUtils ctx(env);
    jlong result;
    ctx.callResultEndpointApi<jlong>(
            &result,
            [&ctx, &thiz, &file_handle, &position]() {
                return (jlong) getGroupApi(ctx, thiz)->seekInEncryptedFile(
                        file_handle,
                        position
                );
            });
    if (ctx->ExceptionCheck()) {
        return 0;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_finishFileDecryption(
        JNIEnv *env,
        jobject thiz,
        jlong file_handle
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &thiz, &file_handle]() {
                group::DecryptedFileInfo info_c = getGroupApi(ctx, thiz)->finishFileDecryption(
                        file_handle
                );
                return privmx::wrapper::decryptedFileInfo2Java(ctx, info_c);
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_subscribeFor(
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
            [&ctx, &thiz, &subscription_queries]() {
                auto subscription_queries_c = jArrayToVector<std::string>(
                        ctx,
                        ctx.jObject2jArray(subscription_queries),
                        jobject2string
                );

                auto subscription_ids_c = getGroupApi(ctx, thiz)->subscribeFor(
                        subscription_queries_c);

                auto arrayList = vectorTojArray(
                        ctx,
                        subscription_ids_c,
                        string2jobject
                );

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
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_unsubscribeFrom(
        JNIEnv *env,
        jobject thiz,
        jobject subscription_ids
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(subscription_ids, "Subscription IDs")) {
        return;
    }

    ctx.callVoidEndpointApi([&ctx, &thiz, &subscription_ids]() {
        auto subscription_ids_c = jArrayToVector<std::string>(
                ctx,
                ctx.jObject2jArray(subscription_ids),
                jobject2string,
                true
        );
        if (ctx->ExceptionCheck()) return;

        getGroupApi(ctx, thiz)->unsubscribeFrom(subscription_ids_c);
    });
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_group_GroupApi_buildSubscriptionQuery(
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
            [&ctx, &thiz, &event_type, &selector_type, &selector_id]() {
                std::string query_result_c = getGroupApi(ctx, thiz)->buildSubscriptionQuery(
                        static_cast<group::EventType>(event_type),
                        static_cast<group::EventSelectorType>(selector_type),
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
