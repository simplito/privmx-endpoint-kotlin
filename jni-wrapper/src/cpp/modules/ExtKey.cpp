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

#include "privmx/endpoint/crypto/ExtKey.hpp"
#include "../utils.hpp"
#include <jni.h>

using namespace privmx::endpoint;

crypto::ExtKey *getExtKey(JniContextUtils &ctx, jobject thiz) {
    jclass cls = ctx->GetObjectClass(thiz);
    jfieldID keyFID = ctx->GetFieldID(cls, "key", "Ljava/lang/Long;");
    jobject keyLong = ctx->GetObjectField(thiz, keyFID);
    if (keyLong == nullptr) {
        throw IllegalStateException("This ExtKey instance cannot be used anymore");
    }
    return (crypto::ExtKey *) ctx.getObject(keyLong).getLongValue();
}

jobject initExtKey(JniContextUtils &ctx, privmx::endpoint::crypto::ExtKey &extKey_c, jclass clazz) {
    jmethodID initExtKeyMID = ctx->GetMethodID(
            clazz, "<init>", "(Ljava/lang/Long;)V");

    auto *key = new privmx::endpoint::crypto::ExtKey(extKey_c);
    return ctx->NewObject(
            clazz,
            initExtKeyMID,
            ctx.long2jLong((jlong) key));
}

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_deinit(
        JNIEnv *env,
        jobject thiz
) {
    try {
        JniContextUtils ctx(env);
        auto key = getExtKey(ctx, thiz);
        delete key;
        jclass cls = env->GetObjectClass(thiz);
        jfieldID keyFID = env->GetFieldID(cls, "key", "Ljava/lang/Long;");
        env->SetObjectField(thiz, keyFID, (jobject) nullptr);
    } catch (const IllegalStateException &e) {
        env->ThrowNew(
                env->FindClass("java/lang/IllegalStateException"),
                e.what()
        );
    }
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_fromSeed(
        JNIEnv *env,
        jclass clazz,
        jbyteArray seed
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(seed, "Seed")) return nullptr;

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &clazz, &seed]() {
                crypto::ExtKey extKey = crypto::ExtKey::fromSeed(
                        core::Buffer::from(ctx.jByteArray2String(seed)));
                return initExtKey(ctx, extKey, clazz);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_fromBase58(
        JNIEnv *env,
        jclass clazz,
        jstring base58
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(base58, "Base58")) return nullptr;

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &clazz, &base58]() {
                crypto::ExtKey extKey = crypto::ExtKey::fromBase58(ctx.jString2string(base58));
                return initExtKey(ctx, extKey, clazz);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_generateRandom(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &clazz]() {
                crypto::ExtKey extKey = crypto::ExtKey::generateRandom();
                return initExtKey(ctx, extKey, clazz);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_derive(
        JNIEnv *env,
        jobject thiz,
        jint index
) {
    JniContextUtils ctx(env);

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &index]() {
                crypto::ExtKey extKey = getExtKey(ctx, thiz)->derive(index);
                jclass cls = env->GetObjectClass(thiz);
                return initExtKey(ctx, extKey, cls);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_deriveHardened(
        JNIEnv *env,
        jobject thiz,
        jint index
) {
    JniContextUtils ctx(env);

    jobject result;
    ctx.callResultEndpointApi<jobject>(
            &result,
            [&ctx, &env, &thiz, &index]() {
                crypto::ExtKey extKey = getExtKey(ctx, thiz)->deriveHardened(index);
                jclass cls = env->GetObjectClass(thiz);
                return initExtKey(ctx, extKey, cls);
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPrivatePartAsBase58(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz]() {
                std::string privatePart = getExtKey(ctx, thiz)->getPrivatePartAsBase58();
                return ctx->NewStringUTF(privatePart.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPublicPartAsBase58(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz]() {
                std::string publicPart = getExtKey(ctx, thiz)->getPublicPartAsBase58();
                return ctx->NewStringUTF(publicPart.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPrivateKey(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz]() {
                std::string privateKey = getExtKey(ctx, thiz)->getPrivateKey();
                return ctx->NewStringUTF(privateKey.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPublicKey(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz]() {
                std::string publicKey = getExtKey(ctx, thiz)->getPublicKey();
                return ctx->NewStringUTF(publicKey.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPrivateEncKey(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &env, &thiz]() {
                core::Buffer privateEncKey = getExtKey(ctx,
                                                       thiz)->getPrivateEncKey();
                jbyteArray array = ctx->NewByteArray(privateEncKey.size());
                ctx->SetByteArrayRegion(array, 0, privateEncKey.size(),
                                        (jbyte *) privateEncKey.data());
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getPublicKeyAsBase58Address(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &env, &thiz]() {
                std::string publicKey = getExtKey(ctx, thiz)->getPublicKeyAsBase58Address();
                return ctx->NewStringUTF(publicKey.c_str());
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_getChainCode(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jbyteArray result;
    ctx.callResultEndpointApi<jbyteArray>(
            &result,
            [&ctx, &env, &thiz]() {
                core::Buffer chainCode = getExtKey(ctx, thiz)->getChainCode();

                jbyteArray array = ctx->NewByteArray(chainCode.size());
                ctx->SetByteArrayRegion(array, 0, chainCode.size(),
                                        (jbyte *) chainCode.data());
                return array;
            }
    );
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_verifyCompactSignatureWithHash(
        JNIEnv *env,
        jobject thiz,
        jbyteArray message,
        jbyteArray signature
) {

    JniContextUtils ctx(env);

    if (ctx.nullCheck(message, "Message") ||
        ctx.nullCheck(signature, "Signature"))
        return JNI_FALSE;;

    jboolean result;
    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &env, &thiz, &message, &signature]() {
                auto response = getExtKey(ctx, thiz)->verifyCompactSignatureWithHash(
                        core::Buffer::from(ctx.jByteArray2String(message)),
                        core::Buffer::from(ctx.jByteArray2String(signature))
                );
                return response ? JNI_TRUE : JNI_FALSE;
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_ExtKey_isPrivate(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);

    jboolean result;
    ctx.callResultEndpointApi<jboolean>(
            &result,
            [&ctx, &thiz]() {
                auto response = getExtKey(ctx, thiz)->isPrivate();
                return response ? JNI_TRUE : JNI_FALSE;
            }
    );

    if (ctx->ExceptionCheck()) {
        return JNI_FALSE;
    }
    return result;
}