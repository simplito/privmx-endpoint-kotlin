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
#include <privmx/endpoint/core/Exception.hpp>
#include "privmx/endpoint/core/Exception.hpp"

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

    jobject getKotlinUnit();

    bool nullCheck(void *value, std::string value_name);


private:
    JNIEnv *_env;
};

#endif //PRIVMX_PRIVMXPOCKETLIB_UTILS_HPP
