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

#ifndef PRIVMX_PRIVMXPOCKETLIB_UTILS_HPP
#define PRIVMX_PRIVMXPOCKETLIB_UTILS_HPP

#include <string>
#include <jni.h>
#include <functional>
#include <privmx/endpoint/core/Exception.hpp>
#include "privmx/endpoint/core/Exception.hpp"
#include "exceptions.h"

class JniContextUtils {
public:
    class Object {
    public:
        Object(JniContextUtils &env, jobject obj);

        ~Object();

        std::string getFieldAsString(const std::string &name);

        jlong getLongValue();

        jboolean getBooleanValue();

    private:
        jobject _obj;
        jclass _objCls;
        JniContextUtils &_env;
    };

    JniContextUtils(JNIEnv *env) : _env(env) {}

    JNIEnv *operator->() { return _env; }

    std::string jString2string(jstring str);

    std::string jByteArray2String(jbyteArray arr);

    jobjectArray jObject2jArray(jobject obj);

    Object getObject(jobject obj);

    jthrowable coreException2jthrowable(privmx::endpoint::core::Exception exception_c);

    jobject long2jLong(long long value);

    jobject bool2jBoolean(bool value);

    jobject int2jInteger(int value);

    bool nullCheck(void *value, std::string value_name);

    template<typename T>
    void callResultEndpointApi(T *result, const std::function<T()> &fun) {
        try {
            *result = fun();
        } catch (const privmx::endpoint::core::Exception &e) {
            _env->Throw(coreException2jthrowable(e));
        } catch (const IllegalStateException &e) {
            _env->ThrowNew(
                    _env->FindClass("java/lang/IllegalStateException"),
                    e.what()
            );
        } catch (const std::exception &e) {
            _env->ThrowNew(
                    _env->FindClass(
                            "com/simplito/java/privmx_endpoint/model/exceptions/NativeException"),
                    e.what()
            );
        } catch (...) {
            _env->ThrowNew(
                    _env->FindClass(
                            "com/simplito/java/privmx_endpoint/model/exceptions/NativeException"),
                    "Unknown exception"
            );
        }
    }

    void callVoidEndpointApi(const std::function<void()> &fun);

private:
    JNIEnv *_env;
};

#endif //PRIVMX_PRIVMXPOCKETLIB_UTILS_HPP
