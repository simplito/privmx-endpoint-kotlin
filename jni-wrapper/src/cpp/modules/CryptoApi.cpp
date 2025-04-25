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
#include <privmx/endpoint/core/Exception.hpp>
#include <privmx/endpoint/crypto/CryptoApi.hpp>
#include "../utils.hpp"
#include "../parser.h"
#include "../exceptions.h"

using namespace privmx::endpoint;

crypto::CryptoApi *getCryptoApi(JNIEnv *env, jobject thiz) {
    JniContextUtils ctx(env);
    jclass cls = ctx->GetObjectClass(thiz);
    jfieldID apiFID = ctx->GetFieldID(cls, "api", "Ljava/lang/Long;");
    jobject apiLong = ctx->GetObjectField(thiz, apiFID);
    if (apiLong == nullptr) {
        throw IllegalStateException("CryptoApi cannot be used");
    }
    return (crypto::CryptoApi *) ctx.getObject(apiLong).getLongValue();
}


extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_init(JNIEnv *env, jobject thiz) {
    JniContextUtils ctx(env);
    try {
        auto cryptoApi = crypto::CryptoApi::create();
        auto cryptoApi_ptr = new crypto::CryptoApi();
        *cryptoApi_ptr = cryptoApi;
        return ctx.long2jLong((jlong) cryptoApi_ptr);
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_deinit(JNIEnv *env, jobject thiz) {
    try {
        //if null go to catch
        auto api = getCryptoApi(env, thiz);
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_generatePrivateKey(
        JNIEnv *env,
        jobject thiz,
        jstring random_seed
) {
    JniContextUtils ctx(env);
    try {
        std::optional<std::string> random_seed_c = std::nullopt;
        if (random_seed != nullptr) {
            random_seed_c = ctx.jString2string(random_seed);
        }
        return env->NewStringUTF(
                getCryptoApi(env, thiz)->generatePrivateKey(
                        random_seed_c
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
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_derivePublicKey(
        JNIEnv *env,
        jobject thiz,
        jstring private_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(private_key, "Private key")) {
        return nullptr;
    }
    try {
        return env->NewStringUTF(
                getCryptoApi(env, thiz)->derivePublicKey(
                        ctx.jString2string(private_key)
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
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_encryptDataSymmetric(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jbyteArray symmetric_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(symmetric_key, "Symmetric key")) {
        return nullptr;
    }
    try {
        auto response = getCryptoApi(env, thiz)->encryptDataSymmetric(
                core::Buffer::from(ctx.jByteArray2String(data)),
                core::Buffer::from(ctx.jByteArray2String(symmetric_key))
        );
        jbyteArray result = env->NewByteArray(response.size());
        env->SetByteArrayRegion(result, 0, response.size(), (jbyte *) response.data());

        return result;
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
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_decryptDataSymmetric(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jbyteArray symmetric_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(symmetric_key, "Symmetric key")) {
        return nullptr;
    }
    try {
        auto response = getCryptoApi(env, thiz)->decryptDataSymmetric(
                core::Buffer::from(ctx.jByteArray2String(data)),
                core::Buffer::from(ctx.jByteArray2String(symmetric_key))
        );
        jbyteArray result = env->NewByteArray(response.size());
        env->SetByteArrayRegion(result, 0, response.size(), (jbyte *) response.data());
        return result;
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
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_signData(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jstring private_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(private_key, "Private key")) {
        return nullptr;
    }
    try {
        auto response = getCryptoApi(env, thiz)->signData(
                core::Buffer::from(ctx.jByteArray2String(data)),
                ctx.jString2string(private_key)
        );

        jbyteArray result = env->NewByteArray(response.size());
        env->SetByteArrayRegion(result, 0, response.size(), (jbyte *) response.data());
        return result;
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_convertPEMKeyToWIFKey(
        JNIEnv *env,
        jobject thiz,
        jstring pem_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(pem_key, "Pem key")) {
        return nullptr;
    }
    try {
        std::string convertedKey = getCryptoApi(env, thiz)->convertPEMKeytoWIFKey(
                ctx.jString2string(pem_key));
        return env->NewStringUTF(convertedKey.c_str());
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_derivePrivateKey(
        JNIEnv *env,
        jobject thiz,
        jstring password,
        jstring salt
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(password, "Password") ||
        ctx.nullCheck(salt, "Salt")) {
        return nullptr;
    }
    try {
        auto result = getCryptoApi(env, thiz)->derivePrivateKey(
                ctx.jString2string(password),
                ctx.jString2string(salt)
        );
        return env->NewStringUTF(result.c_str());
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_derivePrivateKey2(
        JNIEnv *env,
        jobject thiz,
        jstring password,
        jstring salt
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(password, "Password") || ctx.nullCheck(salt, "Salt")) {
        return nullptr;
    }
    try {
        auto result = getCryptoApi(env, thiz)->derivePrivateKey2(
                ctx.jString2string(password),
                ctx.jString2string(salt)
        );
        return env->NewStringUTF(result.c_str());
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
JNIEXPORT jbyteArray JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_generateKeySymmetric(
        JNIEnv *env,
        jobject thiz
) {
    JniContextUtils ctx(env);
    try {
        auto response = getCryptoApi(env, thiz)->generateKeySymmetric();
        jbyteArray result = env->NewByteArray(response.size());
        env->SetByteArrayRegion(result, 0, response.size(), (jbyte *) response.data());
        return result;
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
JNIEXPORT jboolean JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_crypto_CryptoApi_verifySignature(
        JNIEnv *env,
        jobject thiz,
        jbyteArray data,
        jbyteArray signature,
        jstring public_key
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(data, "Data") ||
        ctx.nullCheck(signature, "Signature") ||
        ctx.nullCheck(public_key, "Public key")
            ) {
        return JNI_FALSE;
    }
    try {
        auto response = getCryptoApi(env, thiz)->verifySignature(
                core::Buffer::from(ctx.jByteArray2String(data)),
                core::Buffer::from(ctx.jByteArray2String(signature)),
                ctx.jString2string(public_key)
        );
        return response ? JNI_TRUE : JNI_FALSE;
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

    return JNI_FALSE;
}