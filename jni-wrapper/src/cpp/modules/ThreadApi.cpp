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
#include <privmx/endpoint/thread/ThreadApi.hpp>
#include <privmx/endpoint/core/Exception.hpp>
#include "Connection.h"
#include "ThreadApi.h"
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"
#include "Connection.h"

using namespace privmx::endpoint;

thread::ThreadApi *getThreadApi(JniContextUtils &ctx, jobject threadApiInstance) {
    jclass cls = ctx->GetObjectClass(threadApiInstance);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(threadApiInstance, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("ThreadApi cannot be used");
    }
    return (thread::ThreadApi *) ctx.getObject(apiLong).getLongValue();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_init(
        JNIEnv *env,
        jobject thiz,
        jobject connection
) {
    JniContextUtils ctx(env);
    try {
        auto connection_c = getConnection(env, connection);
        auto threadApi = thread::ThreadApi::create(*connection_c);
        auto threadApi_ptr = new thread::ThreadApi();
        *threadApi_ptr = threadApi;
        return ctx.long2jLong((jlong) threadApi_ptr);
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_deinit(JNIEnv *env,
                                                                        jobject thiz) {
    try {
        JniContextUtils ctx(env);
        //if null go to catch
        auto api = getThreadApi(ctx, thiz);
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_createThread(
        JNIEnv *env, jobject thiz,
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
    try {
        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx,
                ctx.jObject2jArray(users)
        );
        std::vector<core::UserWithPubKey> managers_c = usersToVector(
                ctx,
                ctx.jObject2jArray(managers)
        );
        auto container_policies_opt = std::optional<core::ContainerPolicy>(
                parseContainerPolicy(ctx,container_policies)
        );

        return ctx->NewStringUTF(
                getThreadApi(ctx, thiz)->createThread(
                        ctx.jString2string(context_id),
                        users_c,
                        managers_c,
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        container_policies_opt
                ).c_str()
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_getThread(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID")) {
        return nullptr;
    }
    try {
        thread::Thread thread_c = getThreadApi(ctx, thiz)->getThread(
                ctx.jString2string(thread_id)
        );
        return privmx::wrapper::thread2Java(ctx, thread_c);
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_listThreads(
        JNIEnv *env,
        jobject thiz,
        jstring context_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(context_id, "Context ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }

    jclass pagingListCls = env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/PagingList");
    jmethodID pagingListInitMID = env->GetMethodID(pagingListCls, "<init>",
                                                   "(Ljava/lang/Long;Ljava/util/List;)V"
    );
    jclass arrayCls = env->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = env->GetMethodID(
            arrayCls,
            "<init>",
            "()V"
    );
    jmethodID addToArrayMID = env->GetMethodID(
            arrayCls,
            "add",
            "(Ljava/lang/Object;)Z"
    );
    try {
        auto query = core::PagingQuery();
        query.skip = skip;
        query.limit = limit;
        query.sortOrder = ctx.jString2string(sort_order);
        if (last_id != nullptr) {
            query.lastId = ctx.jString2string(last_id);
        }
        core::PagingList<thread::Thread>
                threads_c = getThreadApi(ctx, thiz)->listThreads(
                ctx.jString2string(context_id),
                query
        );
        jobject array = env->NewObject(arrayCls, initArrayMID);
        for (auto &thread_c: threads_c.readItems) {
            env->CallBooleanMethod(
                    array,
                    addToArrayMID,
                    privmx::wrapper::thread2Java(ctx, thread_c)
            );
        }
        return ctx->NewObject(
                pagingListCls,
                pagingListInitMID,
                ctx.long2jLong(threads_c.totalAvailable),
                array
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_sendMessage(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jbyteArray data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta") ||
        ctx.nullCheck(data, "Data")) {
        return nullptr;
    }
    try {
        return env->NewStringUTF(
                getThreadApi(ctx, thiz)->sendMessage(
                        ctx.jString2string(thread_id),
                        core::Buffer::from(ctx.jByteArray2String(public_meta)),
                        core::Buffer::from(ctx.jByteArray2String(private_meta)),
                        core::Buffer::from(ctx.jByteArray2String(data))
                ).c_str()
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_listMessages(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id,
        jlong skip,
        jlong limit,
        jstring sort_order,
        jstring last_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID") ||
        ctx.nullCheck(sort_order, "Sort order")) {
        return nullptr;
    }
    try {
        jclass pagingListCls = env->FindClass(
                "com/simplito/kotlin/privmx_endpoint/model/PagingList");
        jmethodID pagingListInitMID = env->GetMethodID(pagingListCls, "<init>",
                                                       "(Ljava/lang/Long;Ljava/util/List;)V"
        );
        jclass arrayCls = env->FindClass("java/util/ArrayList");
        jmethodID initArrayMID = env->GetMethodID(arrayCls, "<init>", "()V");
        jmethodID addToArrayMID = env->GetMethodID(arrayCls, "add", "(Ljava/lang/Object;)Z");
        auto query = core::PagingQuery();
        query.skip = skip;
        query.limit = limit;
        query.sortOrder = ctx.jString2string(sort_order);
        if (last_id != nullptr) {
            query.lastId = ctx.jString2string(last_id);
        }
        core::PagingList<thread::Message> messages_c = getThreadApi(ctx, thiz)->listMessages(
                ctx.jString2string(thread_id),
                query
        );
        jobject array = env->NewObject(arrayCls, initArrayMID);
        for (auto &threadMessage_c: messages_c.readItems) {
            env->CallBooleanMethod(array,
                                   addToArrayMID,
                                   privmx::wrapper::message2Java(ctx, threadMessage_c)
            );
        }
        return ctx->NewObject(
                pagingListCls,
                pagingListInitMID,
                ctx.long2jLong(messages_c.totalAvailable),
                array
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_deleteThread(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID")) {
        return;
    }
    try {
        getThreadApi(ctx, thiz)->deleteThread(
                ctx.jString2string(thread_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_deleteMessage(
        JNIEnv *env,
        jobject thiz,
        jstring message_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(message_id, "Message ID")) {
        return;
    }
    try {
        getThreadApi(ctx, thiz)->deleteMessage(
                ctx.jString2string(message_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_updateThread(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id,
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
    if (ctx.nullCheck(thread_id, "Thread ID") ||
        ctx.nullCheck(users, "Users list") ||
        ctx.nullCheck(managers, "Managers list") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta")) {
        return;
    }
    try {
        std::vector<core::UserWithPubKey> users_c = usersToVector(
                ctx,
                ctx.jObject2jArray(users)
        );
        std::vector<core::UserWithPubKey> managers_c = usersToVector(
                ctx,
                ctx.jObject2jArray(managers)
        );
        auto container_policies_opt = std::optional<core::ContainerPolicy>(
                parseContainerPolicy(ctx,container_policies)
        );
        getThreadApi(ctx, thiz)->updateThread(
                ctx.jString2string(thread_id),
                users_c,
                managers_c,
                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                version,
                force == JNI_TRUE,
                force_generate_new_key == JNI_TRUE,
                container_policies_opt
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_getMessage(
        JNIEnv *env,
        jobject thiz,
        jstring message_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(message_id, "Message ID")) {
        return nullptr;
    }
    try {
        return privmx::wrapper::message2Java(
                ctx,
                getThreadApi(ctx, thiz)->getMessage(
                        ctx.jString2string(message_id)
                )
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_updateMessage(
        JNIEnv *env,
        jobject thiz,
        jstring message_id,
        jbyteArray public_meta,
        jbyteArray private_meta,
        jbyteArray data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(message_id, "Message ID") ||
        ctx.nullCheck(public_meta, "Public meta") ||
        ctx.nullCheck(private_meta, "Private meta") ||
        ctx.nullCheck(data, "Data")) {
        return;
    }
    try {
        getThreadApi(ctx, thiz)->updateMessage(
                ctx.jString2string(message_id),
                core::Buffer::from(ctx.jByteArray2String(public_meta)),
                core::Buffer::from(ctx.jByteArray2String(private_meta)),
                core::Buffer::from(ctx.jByteArray2String(data))
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_subscribeForThreadEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    try {
        getThreadApi(ctx, thiz)->subscribeForThreadEvents();
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_unsubscribeFromThreadEvents(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    try {
        getThreadApi(ctx, thiz)->unsubscribeFromThreadEvents();
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_subscribeForMessageEvents(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID")) {
        return;
    }
    try {
        getThreadApi(ctx, thiz)->subscribeForMessageEvents(
                ctx.jString2string(thread_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_thread_ThreadApi_unsubscribeFromMessageEvents(
        JNIEnv *env,
        jobject thiz,
        jstring thread_id
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(thread_id, "Thread ID")) {
        return;
    }
    try {
        getThreadApi(ctx, thiz)->unsubscribeFromMessageEvents(
                ctx.jString2string(thread_id)
        );
    } catch (const core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
    } catch (const IllegalStateException &e) {
        ctx->ThrowNew(
                ctx->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    } catch (const std::exception &e) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        env->ThrowNew(
                env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}