//
// Created by Dawid Jenczewski on 13/02/2025.
//

#include "WebRTCInterfaceJNI.h"
#include "../jniUtils.h"
#include "../utils.hpp"
#include "../model_native_initializers.h"

#include <thread>

JNIEnv *WebRTCInterfaceJNI::AttachCurrentThreadIfNeeded() {
    JNIEnv *jni = nullptr;
    jint status = javaVM->GetEnv((void **) &jni, JNI_VERSION_1_6);
    //return if current thread is attached
    if (jni != nullptr && status == JNI_OK) return jni;

    std::string name(
            "WebRTCInterfaceJNI - " + std::to_string(
                    std::hash<std::thread::id>{}(std::this_thread::get_id())));
    JavaVMAttachArgs args;
    args.version = JNI_VERSION_1_6;
    args.name = &name[0];
    args.group = nullptr;
#ifdef _JAVASOFT_JNI_H_  // Oracle's jni.h violates the JNI spec!
    void* env = nullptr;
#else
    JNIEnv *env = nullptr;
#endif
    //TODO: Attached thread should be also detached
    if (javaVM->AttachCurrentThread(&env, &args) == JNI_OK) {
        return reinterpret_cast<JNIEnv *>(env);
    }
    return nullptr;
}

WebRTCInterfaceJNI::WebRTCInterfaceJNI(JNIEnv *env, jobject jwebRTCInterface) {
    jclass jwebRTCInterfaceClass = env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/modules/stream/WebRTCInterface");
    javaVM = nullptr;
    if (!env->IsInstanceOf(jwebRTCInterface, jwebRTCInterfaceClass)) {
        env->ThrowNew(
                env->FindClass("java/lang/IllegalArgumentException"),
                "WebRTCInterfaceJNI::WebRTCInterfaceJNI object must be instance of WebRTCInterface");
        return;
    }
    env->GetJavaVM(&this->javaVM);
    //TODO: Clean this global ref on close()
    this->jwebRTCInterface = env->NewGlobalRef(jwebRTCInterface);
}

std::string WebRTCInterfaceJNI::createOfferAndSetLocalDescription(
        const std::string &streamRoomId,
        const std::string &connectionType
) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    JniContextUtils ctx(env);
//    env->ThrowNew(
//            env->FindClass("java/lang/NullPointerException"),
//            "createOfferAndSetLocalDescription"
//    );
//    env->ExceptionDescribe();
//    return  "";
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "createOfferAndSetLocalDescription",
            "("
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            ")"
            "Ljava/lang/String;"
    );

    auto result = (jstring) env->CallObjectMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            env->NewStringUTF(connectionType.c_str())
    );
    if (result == nullptr) {
        env->ThrowNew(
                env->FindClass("java/lang/NullPointerException"),
                "WebRTCInterfaceJni::createOfferAndSetLocalDescription cannot return null"
        );
        return {};
    }
    return ctx.jString2string(result);
}

std::string WebRTCInterfaceJNI::createAnswerAndSetDescriptions(
        const std::string &streamRoomId,
        const std::string &sdp,
        const std::string &type,
        const std::string &connectionType
) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    JniContextUtils ctx(env);
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "createAnswerAndSetDescriptions",
            "("
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            ")Ljava/lang/String;"
    );
    auto result = (jstring) env->CallObjectMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            env->NewStringUTF(sdp.c_str()),
            env->NewStringUTF(type.c_str()),
            env->NewStringUTF(connectionType.c_str())
    );

    if (result == nullptr) {
        env->ThrowNew(
                env->FindClass("java/lang/NullPointerException"),
                "WebRTCInterfaceJni::createAnswerAndSetDescriptions cannot return null");
        return "";
    }
    return ctx.jString2string(result);
}

void WebRTCInterfaceJNI::setAnswerAndSetRemoteDescription(
        const std::string &streamRoomId,
        const std::string &sdp,
        const std::string &type,
        const std::string &connectionType
) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    JniContextUtils ctx(env);
//    env->ThrowNew(
//            env->FindClass("java/lang/NullPointerException"),
//            "setAnswerAndSetRemoteDescription"
//    );
//    env->ExceptionDescribe();
//    return;
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "setAnswerAndSetRemoteDescription",
            "("
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            ")V"
    );
    env->CallVoidMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            env->NewStringUTF(sdp.c_str()),
            env->NewStringUTF(type.c_str()),
            env->NewStringUTF(connectionType.c_str())
    );
}

void WebRTCInterfaceJNI::updateSessionId(
        const std::string &streamRoomId,
        const int64_t sessionId,
        const std::string &connectionType
) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    JniContextUtils ctx(env);
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "updateSessionId",
            "("
            "Ljava/lang/String;"
            "Ljava/lang/Long;"
            "Ljava/lang/String;"
            ")V"
    );
    env->CallVoidMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            ctx.long2jLong(sessionId),
            env->NewStringUTF(connectionType.c_str())
    );
}

void WebRTCInterfaceJNI::close(
        const std::string &streamRoomId,
        const std::string& connectionType
        ) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "close",
            "("
            "Ljava/lang/String;"
            "Ljava/lang/String;"
            ")V"
    );
    env->CallVoidMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            env->NewStringUTF(connectionType.c_str())
    );
}

void WebRTCInterfaceJNI::closeAll(const std::string &streamRoomId) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "closeAll",
            "("
            "Ljava/lang/String;"
            ")V"
    );
    env->CallVoidMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str())
    );
}

void WebRTCInterfaceJNI::updateKeys(
        const std::string &streamRoomId,
        const std::vector<Key> &keys
) {
    JNIEnv *env = AttachCurrentThreadIfNeeded();
    JniContextUtils ctx(env);
    jclass jwebRTCInterfaceClass = env->GetObjectClass(jwebRTCInterface);
    jmethodID jmethodId = env->GetMethodID(
            jwebRTCInterfaceClass,
            "updateKeys",
            "("
            "Ljava/lang/String;"
            "Ljava/util/List;"
            ")V"
    );
    jclass arrayCls = env->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = env->GetMethodID(
            arrayCls,
            "<init>",
            "()V"
    );
    jobject jKeysArray = env->NewObject(arrayCls, initArrayMID);
    jmethodID addToArrayMID = env->GetMethodID(
            arrayCls,
            "add",
            "(Ljava/lang/Object;)Z"
    );
    ctx.setClassLoaderFromObject(jwebRTCInterface);
    for (auto &key_c: keys) {
        env->CallBooleanMethod(
                jKeysArray,
                addToArrayMID,
                privmx::wrapper::key2Java(ctx, key_c)
        );
    }
    env->CallVoidMethod(
            jwebRTCInterface,
            jmethodId,
            env->NewStringUTF(streamRoomId.c_str()),
            jKeysArray
    );
}

//std::string WebRTCInterfaceJNI::createOfferAndSetLocalDescription(
//        const std::string &streamRoomId
//) {
//    return std::string();
//}
//
//std::string WebRTCInterfaceJNI::createAnswerAndSetDescriptions(
//        const std::string &streamRoomId,
//        const std::string &sdp,
//        const std::string &type
//) {
//    return std::string();
//}
//
//void WebRTCInterfaceJNI::setAnswerAndSetRemoteDescription(
//        const std::string &streamRoomId,
//        const std::string &sdp,
//        const std::string &type
//) {
//
//}
//
//void WebRTCInterfaceJNI::updateSessionId(
//        const std::string &streamRoomId,
//        const int64_t sessionId,
//        const std::string &connectionType
//) {
//
//}
//
//void WebRTCInterfaceJNI::close(
//        const std::string &streamRoomId
//) {
//
//}
//
//void WebRTCInterfaceJNI::updateKeys(
//        const std::string &streamRoomId,
//        const std::vector<Key> &keys
//) {
//
//}
