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

#include "UserVerifierInterfaceJNI.h"
#include "../model_native_initializers.h"
#include "../utils.hpp"
#include "../jniUtils.h"
#include <thread>
#include <jni.h>
#include <iostream>
#include <memory>

privmx::wrapper::UserVerifierInterfaceJNI::UserVerifierInterfaceJNI(
        JNIEnv *env,
        jobject juserVerifierInterface
) {
    jclass juserVerifierInterfaceClass = env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/modules/core/UserVerifierInterface");
    javaVM = nullptr;
    this->juserVerifierInterface = nullptr;
    if (!env->IsInstanceOf(juserVerifierInterface, juserVerifierInterfaceClass)) {
        env->ThrowNew(
                env->FindClass("java/lang/IllegalArgumentException"),
                "UserVerifierInterfaceJNI::UserVerifierInterfaceJNI object must be instance of UserVerifierInterface");
        return;
    }
    env->GetJavaVM(&this->javaVM);
    this->juserVerifierInterface = env->NewGlobalRef(juserVerifierInterface);
}

std::vector<bool>
privmx::wrapper::UserVerifierInterfaceJNI::verify(
        const std::vector<privmx::endpoint::core::VerificationRequest> &request
) {
    JNIEnv *env = privmx::wrapper::jni::AttachCurrentThreadIfNeeded(
            javaVM,
            jni::getPrivmxCallbackThreadName());
    JniContextUtils ctx(env);
    ctx.setClassLoaderFromObject(juserVerifierInterface);
    jclass juserVerifierInterfaceClass = env->GetObjectClass(juserVerifierInterface);
    jmethodID jverifyMID = env->GetMethodID(
            juserVerifierInterfaceClass,
            "verify",
            "(Ljava/util/List;)Ljava/util/List;");

    jclass arrayClass = env->FindClass("java/util/ArrayList");
    jmethodID initArrayMID = env->GetMethodID(
            arrayClass,
            "<init>",
            "()V");

    jobject jverificationRequestArray = env->NewObject(arrayClass, initArrayMID);
    jmethodID addToArrayMID = env->GetMethodID(
            arrayClass,
            "add",
            "(Ljava/lang/Object;)Z");

    for (auto &request_c: request) {
        env->CallBooleanMethod(jverificationRequestArray,
                               addToArrayMID,
                               privmx::wrapper::verificationRequest2Java(ctx, request_c));
    }

    auto jResult = env->CallObjectMethod(
            juserVerifierInterface,
            jverifyMID,
            jverificationRequestArray
    );

    if (env->ExceptionCheck()) {
        return std::vector<bool>(request.size(), true);
    }

    if (jResult == nullptr) {
        env->ThrowNew(
                env->FindClass("java/lang/NullPointerException"),
                "UserVerifierInterface::verify: The method was expected to return a non-null list, "
                "but returned null instead. Please verify the logic to ensure a valid list is always returned."
        );
        return std::vector<bool>(request.size(), true);
    }

    auto jArray = ctx.jObject2jArray(jResult);
    std::vector<bool> result_c;
    for (int i = 0; i < ctx->GetArrayLength(jArray); i++) {
        jobject jElement = ctx->GetObjectArrayElement(jArray, i);
        if (jElement == nullptr) {
            env->ThrowNew(
                    env->FindClass("java/lang/NullPointerException"),
                    "UserVerifierInterface::verify: "
                    "The method was expected to return a list of non-null elements, but at least one element is null. "
                    "Please verify the logic to ensure a valid result is always returned."
            );
            return std::vector<bool>(request.size(), true);
        }
        bool element_c = ctx.getObject(jElement).getBooleanValue();
        result_c.push_back(element_c);
    }
    return result_c;
}

privmx::wrapper::UserVerifierInterfaceJNI::~UserVerifierInterfaceJNI() {
    if (javaVM != nullptr && juserVerifierInterface != nullptr) {
        JNIEnv *env = privmx::wrapper::jni::AttachCurrentThreadIfNeeded(
                javaVM,
                jni::getPrivmxCallbackThreadName()
        );

        if (env != nullptr) env->DeleteGlobalRef(juserVerifierInterface);
        juserVerifierInterface = nullptr;
        javaVM = nullptr;
    }
}
