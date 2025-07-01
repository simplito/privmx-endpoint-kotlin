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
#include <privmx/endpoint/core/EventQueue.hpp>
#include "../utils.hpp"
#include "../parser.h"

using namespace privmx::endpoint::core;

extern "C"
JNIEXPORT void JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_EventQueue_emitBreakEvent(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);
    ctx.callVoidEndpointApi([]() {
        EventQueue::getInstance().emitBreakEvent();
    });
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_EventQueue_waitEvent(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx]() {
        return parseEvent(ctx, EventQueue::getInstance().waitEvent().get());
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_EventQueue_getEvent(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);
    jobject result;
    ctx.callResultEndpointApi<jobject>(&result, [&ctx]() {
        auto eventHolder = EventQueue::getInstance().getEvent();
        return !eventHolder.has_value() ?
               nullptr :
               parseEvent(ctx, eventHolder.value().get());
    });
    if (ctx->ExceptionCheck()) {
        return nullptr;
    }
    return result;
}