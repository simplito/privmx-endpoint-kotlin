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
#include "../exceptions.h"
#include "../utils.hpp"
#include <privmx/endpoint/core/BackendRequester.hpp>
#include <privmx/endpoint/core/Exception.hpp>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_BackendRequester_backendRequest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(
        JNIEnv *env,
        jclass clazz,
        jstring server_url,
        jstring access_token,
        jstring method,
        jstring params_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(server_url, "Server URL") ||
        ctx.nullCheck(access_token, "Access token") ||
        ctx.nullCheck(method, "Method") ||
        ctx.nullCheck(params_as_json, "Params as json")) {
        return nullptr;
    }
    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &server_url, &method, &params_as_json, &access_token]() {
                return ctx->NewStringUTF(
                        privmx::endpoint::core::BackendRequester::backendRequest(
                                ctx.jString2string(server_url),
                                ctx.jString2string(access_token),
                                ctx.jString2string(method),
                                ctx.jString2string(params_as_json)
                        ).c_str());
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_BackendRequester_backendRequest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(
        JNIEnv *env,
        jclass clazz,
        jstring server_url,
        jstring method,
        jstring params_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(server_url, "Server URL") ||
        ctx.nullCheck(method, "Method") ||
        ctx.nullCheck(params_as_json, "Params as json")) {
        return nullptr;
    }
    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &server_url, &method, &params_as_json]() {
                return ctx->NewStringUTF(
                        privmx::endpoint::core::BackendRequester::backendRequest(
                                ctx.jString2string(server_url),
                                ctx.jString2string(method),
                                ctx.jString2string(params_as_json)
                        ).c_str()
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_BackendRequester_backendRequest__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2JLjava_lang_String_2Ljava_lang_String_2(
        JNIEnv *env,
        jclass clazz,
        jstring server_url,
        jstring api_key_id,
        jstring api_key_secret,
        jlong mode,
        jstring method,
        jstring params_as_json
) {
    JniContextUtils ctx(env);
    if (ctx.nullCheck(server_url, "Server URL") ||
        ctx.nullCheck(api_key_id, "Api key ID") ||
        ctx.nullCheck(api_key_secret, "Api key secret") ||
        ctx.nullCheck(method, "Method") ||
        ctx.nullCheck(params_as_json, "Params as json")) {
        return nullptr;
    }
    jstring result;
    ctx.callResultEndpointApi<jstring>(
            &result,
            [&ctx, &server_url, &method, &params_as_json, &api_key_id, &api_key_secret, &mode]() {
                return ctx->NewStringUTF(
                        privmx::endpoint::core::BackendRequester::backendRequest(
                                ctx.jString2string(server_url),
                                ctx.jString2string(api_key_id),
                                ctx.jString2string(api_key_secret),
                                mode,
                                ctx.jString2string(method),
                                ctx.jString2string(params_as_json)
                        ).c_str()
                );
            });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}