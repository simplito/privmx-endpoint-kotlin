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

#include "utils.hpp"

void replace_all(std::string &input, const std::string &from, const std::string &to);

std::string JniContextUtils::jString2string(jstring str) {
    const char *tmp = _env->GetStringUTFChars(str, NULL);
    std::string result(tmp);
    _env->ReleaseStringUTFChars(str, tmp);
    return result;
}

std::string JniContextUtils::jByteArray2String(jbyteArray arr) {
    jsize size = _env->GetArrayLength(arr);
    jbyte *bytes = _env->GetByteArrayElements(arr, NULL);
    std::string result((const char *) bytes, size);
    _env->ReleaseByteArrayElements(arr, bytes, JNI_ABORT);
    return result;
}

jobjectArray JniContextUtils::jObject2jArray(jobject obj) {
    jclass objClass = _env->GetObjectClass(obj);
    if (_env->IsInstanceOf(obj, _env->FindClass("java/util/List"))) {
        jmethodID mToArray = _env->GetMethodID(objClass, "toArray", "()[Ljava/lang/Object;");

        if (mToArray == nullptr) return nullptr;
        return (jobjectArray) _env->CallObjectMethod(obj, mToArray);
    } else return nullptr;
}

jobject JniContextUtils::long2jLong(long long value) {
    jclass longCls = _env->FindClass("java/lang/Long");
    jmethodID longInitMethodID = _env->GetMethodID(longCls, "<init>", "(J)V");
    return _env->NewObject(longCls, longInitMethodID, (jlong) value);
}

jobject JniContextUtils::bool2jBoolean(bool value) {
    jclass longCls = _env->FindClass("java/lang/Boolean");
    jmethodID longInitMethodID = _env->GetMethodID(longCls, "<init>", "(Z)V");
    return _env->NewObject(longCls, longInitMethodID, (jboolean) value);
}

jobject JniContextUtils::int2jInteger(int value) {
    jclass longCls = _env->FindClass("java/lang/Integer");
    jmethodID longInitMethodID = _env->GetMethodID(longCls, "<init>", "(I)V");
    return _env->NewObject(longCls, longInitMethodID, (jint) value);
}

JniContextUtils::Object JniContextUtils::getObject(jobject obj) {
    return JniContextUtils::Object(*this, obj);
}

jthrowable
JniContextUtils::coreException2jthrowable(privmx::endpoint::core::Exception exception_c) {
    jclass exceptionCls = _env->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/exceptions/PrivmxException");
    jmethodID initExceptionMID = _env->GetMethodID(exceptionCls, "<init>",
                                                   "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");
    return (jthrowable) _env->NewObject(
            exceptionCls,
            initExceptionMID,
            _env->NewStringUTF(exception_c.what()),
            _env->NewStringUTF(exception_c.getDescription().c_str()),
            _env->NewStringUTF(exception_c.getScope().c_str()),
            (int) exception_c.getCode()
    );
}

JniContextUtils::Object::Object(JniContextUtils &env, jobject obj) : _env(env), _obj(obj) {
    _objCls = _env->GetObjectClass(obj);
}

JniContextUtils::Object::~Object() {
    _env->DeleteLocalRef(_objCls);
}

std::string JniContextUtils::Object::getFieldAsString(const std::string &name) {
    jstring jValue = (jstring) _env->GetObjectField(_obj, _env->GetFieldID(_objCls, name.c_str(),
                                                                           "Ljava/lang/String;"));
    std::string value = _env.jString2string(jValue);
    _env->DeleteLocalRef(jValue);
    return value;
}

jlong JniContextUtils::Object::getLongValue() {
    jmethodID longValue = _env->GetMethodID(_objCls, "longValue", "()J");
    return _env->CallLongMethod(_obj, longValue);
}

jboolean JniContextUtils::Object::getBooleanValue() {
    jmethodID booleanValue = _env->GetMethodID(_objCls, "booleanValue", "()Z");
    return _env->CallBooleanMethod(_obj, booleanValue);
}

bool JniContextUtils::nullCheck(void *value, std::string value_name) {
    if (value == nullptr) {
        _env->ThrowNew(
                _env->FindClass("java/lang/NullPointerException"),
                (value_name + " cannot be null").c_str()
        );
        return true;
    }
    return false;
}

jobject JniContextUtils::getKotlinUnit() {
    jclass unitCls = _env->FindClass("kotlin/Unit");
    jfieldID unitInstanceFID = _env->GetStaticFieldID(unitCls, "INSTANCE", "Lkotlin/Unit;");
    return _env->GetStaticObjectField(unitCls, unitInstanceFID);
}

void JniContextUtils::callVoidEndpointApi(const std::function<void()> &fun) {
    try {
        fun();
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
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                e.what()
        );
    } catch (...) {
        _env->ThrowNew(
                _env->FindClass(
                        "com/simplito/kotlin/privmx_endpoint/model/exceptions/NativeException"),
                "Unknown exception"
        );
    }
}



jclass JniContextUtils::findClass(const char *name) {
    if (jclassLoader == nullptr) {
        return _env->FindClass(name);
    }
    std::string nameStr(name);
    replace_all(nameStr, "/", ".");
    const char *binaryName = nameStr.c_str();
    auto classLoaderClass = _env->FindClass("java/lang/ClassLoader");
    auto gFindClassMethod = _env->GetMethodID(
            classLoaderClass,
            "loadClass",
            "(Ljava/lang/String;)Ljava/lang/Class;");
    return static_cast<jclass>(_env->CallObjectMethod(
            jclassLoader,
            gFindClassMethod,
            _env->NewStringUTF(binaryName)));
}

void JniContextUtils::setClassLoaderFromObject(jobject object) {
    auto objectClass = _env->GetObjectClass(object);
    jclass classClass = _env->GetObjectClass(objectClass);
    auto getClassLoaderMethod = _env->GetMethodID(
            classClass,
            "getClassLoader",
            "()Ljava/lang/ClassLoader;");
    jclassLoader = _env->CallObjectMethod(objectClass, getClassLoaderMethod);
}

void replace_all(std::string &input, const std::string &from, const std::string &to) {
    size_t pos = 0;
    while ((pos = input.find(from, pos)) != std::string::npos) {
        input.replace(pos, from.size(), to);
        pos += to.size();
    }
}