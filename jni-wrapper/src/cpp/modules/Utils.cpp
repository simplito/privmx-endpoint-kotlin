//
// Created by Dominika on 05/05/2025.
//

#include "../utils.hpp"
#include <jni.h>
#include <privmx/endpoint/core/Utils.hpp>

// Utils
extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Utils_trim(
        JNIEnv *env,
        jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &clazz, &data]() {
                auto response = privmx::endpoint::core::Utils::trim(ctx.jString2string(data));
                return ctx->NewStringUTF(response.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Utils_split(
        JNIEnv *env,
        jclass clazz,
        jstring data,
        jstring delimiter
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(delimiter, "Delimeter"))
        return nullptr;

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &data, &delimiter]() {
                auto response = privmx::endpoint::core::Utils::split(
                        ctx.jString2string(data),
                        ctx.jString2string(delimiter));

                jclass arrayListCls = env->FindClass("java/util/ArrayList");
                jmethodID initMID = env->GetMethodID(arrayListCls, "<init>", "()V");
                jmethodID addToListMID = env->GetMethodID(arrayListCls, "add",
                                                          "(Ljava/lang/Object;)Z");
                jobject array = env->NewObject(arrayListCls, initMID);

                for (auto &value: response) {
                    env->CallBooleanMethod(
                            array,
                            addToListMID,
                            ctx->NewStringUTF(value.c_str())
                    );
                }

                return array;
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Utils_ltrim(
        JNIEnv *env,
        jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;
    jstring result;

    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &data]() {
                std::string data_n = ctx.jString2string(data);
                privmx::endpoint::core::Utils::ltrim(data_n);
                return ctx->NewStringUTF(data_n.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Utils_rtrim(
        JNIEnv *env,
        jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;
    jstring result;

    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &data]() {
                std::string data_n = ctx.jString2string(data);
                privmx::endpoint::core::Utils::rtrim(data_n);
                return ctx->NewStringUTF(data_n.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}


// Hex
extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Hex_encode(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &data]() {
                auto encoded = privmx::endpoint::core::Hex::encode(
                        privmx::endpoint::core::Buffer::from(ctx.jByteArray2String(data)));
                return ctx->NewStringUTF(encoded.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Hex_decode(
        JNIEnv *env,
        jclass clazz,
        jstring hex_data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(hex_data, "Data")) return nullptr;

    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &hex_data]() {
                auto data = privmx::endpoint::core::Hex::decode(ctx.jString2string(hex_data));

                jbyteArray decoded = ctx->NewByteArray(data.size());
                ctx->SetByteArrayRegion(decoded, 0, data.size(), (jbyte *) data.data());
                return decoded;
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Hex_is(
        JNIEnv *env, jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return JNI_FALSE;
    jboolean result;

    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &data]() {
                auto response = privmx::endpoint::core::Hex::is(ctx.jString2string(data));
                return response ? JNI_TRUE : JNI_FALSE;
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}


// Base32
extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base32_encode(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &data]() {
                auto encoded = privmx::endpoint::core::Base32::encode(
                        privmx::endpoint::core::Buffer::from(ctx.jByteArray2String(data)));
                return ctx->NewStringUTF(encoded.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base32_decode(
        JNIEnv *env,
        jclass clazz,
        jstring base32_data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(base32_data, "Data")) return nullptr;

    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &base32_data]() {
                auto data = privmx::endpoint::core::Base32::decode(ctx.jString2string(base32_data));

                jbyteArray decoded = ctx->NewByteArray(data.size());
                ctx->SetByteArrayRegion(decoded, 0, data.size(), (jbyte *) data.data());
                return decoded;
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base32_is(
        JNIEnv *env,
        jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return JNI_FALSE;
    jboolean result;

    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &data]() {
                auto response = privmx::endpoint::core::Base32::is(ctx.jString2string(data));
                return response ? JNI_TRUE : JNI_FALSE;
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}


// Base64
extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base64_encode(
        JNIEnv *env,
        jclass clazz,
        jbyteArray data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return nullptr;

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &data]() {
                auto encoded = privmx::endpoint::core::Base64::encode(
                        privmx::endpoint::core::Buffer::from(ctx.jByteArray2String(data)));
                return ctx->NewStringUTF(encoded.c_str());
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base64_decode(
        JNIEnv *env,
        jclass clazz,
        jstring base64_data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(base64_data, "Data")) return nullptr;

    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &base64_data]() {
                auto data = privmx::endpoint::core::Base64::decode(ctx.jString2string(base64_data));

                jbyteArray decoded = ctx->NewByteArray(data.size());
                ctx->SetByteArrayRegion(decoded, 0, data.size(), (jbyte *) data.data());
                return decoded;
            }
    );

    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_utils_Base64_is(
        JNIEnv *env,
        jclass clazz,
        jstring data
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data")) return JNI_FALSE;
    jboolean result;

    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &data]() {
                auto response = privmx::endpoint::core::Base64::is(ctx.jString2string(data));
                return response ? JNI_TRUE : JNI_FALSE;
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}