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

//
// Created by Dawid Jenczewski on 29/08/2024.
//
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
    try {
        EventQueue::getInstance().emitBreakEvent();
    } catch (const privmx::endpoint::core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_EventQueue_waitEvent(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);
    try {
        return parseEvent(ctx,EventQueue::getInstance().waitEvent().get());
    } catch (const privmx::endpoint::core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
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
Java_com_simplito_kotlin_privmx_1endpoint_modules_core_EventQueue_getEvent(
        JNIEnv *env,
        jclass clazz
) {
    JniContextUtils ctx(env);
    try {
        auto eventHolder = EventQueue::getInstance().getEvent();
        if(!eventHolder.has_value()) return nullptr;
        return parseEvent(ctx,eventHolder.value().get());
    } catch (const privmx::endpoint::core::Exception &e) {
        env->Throw(ctx.coreException2jthrowable(e));
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