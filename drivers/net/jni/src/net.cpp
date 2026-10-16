#include <jni.h>
#include <atomic>
#include <cstdlib>
#include <cstring>

#pragma GCC visibility push(default)
#include "privmx/drv/net.h"
extern "C" int privmxDrvNet_close();
#pragma GCC visibility pop

#define NET "com/simplito/kotlin/drivers/PrivmxDrvNet"

namespace {
constexpr int OK = 0;
constexpr int ERR_NULL_ARG = 1;
constexpr int ERR_NOT_INITIALIZED = 2;
constexpr int ERR_JVM_EXCEPTION = 3;
constexpr int ERR_JNI_CALL = 4;
constexpr int ERR_BAD_HANDLE = 5;

constexpr const char* T_GROUP_NAME = "privmx-net-drv";
constexpr const char* DEFAULT_CONTENT_TYPE = "application/octet-stream";

enum Cls {
    HTTP,
    WS,
    LISTENER,
    HTTP_OPTIONS,
    HTTP_HEADER,
    HTTP_REQUEST_OPTIONS,
    HTTP_RESPONSE,
    WS_OPTIONS,
    CLS_COUNT,
};

constexpr const char* CLASS_NAMES[CLS_COUNT] = {
    NET "$HttpSession",
    NET "$WebSocket",
    NET "$WebSocket$WebsocketListener",
    NET "$HttpOptions",
    NET "$HttpHeader",
    NET "$HttpRequestOptions",
    NET "$HttpResponse",
    NET "$WsOptions",
};

std::atomic<JavaVM*> g_vm{nullptr};
jclass g_cls[CLS_COUNT] = {};

struct NativeListener {
    void (*onOpen)(void* ctx);
    void (*onMessage)(void* ctx, const char* msg, int msglen);
    void (*onError)(void* ctx, const char* msg, int msglen);
    void (*onClose)(void* ctx, int wasClean);
    void* ctx;
    std::atomic<int> refs;
};

JNIEnv* attach() {
    JavaVM* vm = g_vm.load();
    if (vm == nullptr) return nullptr;
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK) return env;

    JavaVMAttachArgs args{JNI_VERSION_1_6, const_cast<char*>(T_GROUP_NAME), nullptr};
#ifdef __ANDROID__

    jint rc = vm->AttachCurrentThread(&env, &args);
#else
    jint rc = vm->AttachCurrentThread(reinterpret_cast<void**>(&env), &args);
#endif
    return rc == JNI_OK ? env : nullptr;
}

bool exceptionOccurred(JNIEnv* env) {
    if (!env->ExceptionCheck()) return false;
    env->ExceptionDescribe();
    env->ExceptionClear();
    return true;
}

struct LocalFrame {
    JNIEnv* env;
    bool ok;
    LocalFrame(JNIEnv* e, jint capacity) : env(e), ok(e->PushLocalFrame(capacity) == JNI_OK) {}
    ~LocalFrame() {
        if (ok) env->PopLocalFrame(nullptr);
    }
};

jstring newString(JNIEnv* env, const char* text) {
    return env->NewStringUTF(text != nullptr ? text : "");
}

jbyteArray newByteArray(JNIEnv* env, const char* src, int len) {
    if (len < 0) len = 0;
    jbyteArray arr = env->NewByteArray(len);
    if (arr != nullptr && len > 0 && src != nullptr) {
        env->SetByteArrayRegion(arr, 0, len, reinterpret_cast<const jbyte*>(src));
    }
    return arr;
}

char* readByteArray(JNIEnv* env, jbyteArray arr, int* len) {
    *len = 0;
    jsize size = env->GetArrayLength(arr);
    if (size <= 0) return nullptr;
    auto* buf = static_cast<char*>(malloc(size));
    if (buf == nullptr) return nullptr;
    env->GetByteArrayRegion(arr, 0, size, reinterpret_cast<jbyte*>(buf));
    *len = size;
    return buf;
}

jobject newClient(JNIEnv* env, jclass cls, const char* url) {
    jmethodID ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;)V");
    if (ctor == nullptr) return nullptr;
    jobject local = env->NewObject(cls, ctor, newString(env, url));
    if (exceptionOccurred(env) || local == nullptr) return nullptr;
    return env->NewGlobalRef(local);
}

jobject httpRequestOptionsToJava(JNIEnv* env, const privmxDrvNet_HttpRequestOptions* o) {
    jclass cls = g_cls[HTTP_REQUEST_OPTIONS];
    jclass headerCls = g_cls[HTTP_HEADER];
    jmethodID ctor = env->GetMethodID(
        cls, "<init>",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;IZ)V");
    if (ctor == nullptr) return nullptr;
    jmethodID headerCtor =
        env->GetMethodID(headerCls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (headerCtor == nullptr) return nullptr;

    jclass listCls = env->FindClass("java/util/ArrayList");
    if (listCls == nullptr) return nullptr;
    jmethodID listCtor = env->GetMethodID(listCls, "<init>", "()V");
    if (listCtor == nullptr) return nullptr;
    jmethodID listAdd = env->GetMethodID(listCls, "add", "(Ljava/lang/Object;)Z");
    if (listAdd == nullptr) return nullptr;
    jobject list = env->NewObject(listCls, listCtor);
    if (list == nullptr) return nullptr;

    for (int i = 0; o->headers != nullptr && i < o->headerslen; ++i) {
        jstring name = newString(env, o->headers[i].name);
        jstring value = newString(env, o->headers[i].value);
        jobject header = env->NewObject(headerCls, headerCtor, name, value);
        if (header != nullptr) env->CallBooleanMethod(list, listAdd, header);
        if (env->ExceptionCheck()) return nullptr;

        env->DeleteLocalRef(name);
        env->DeleteLocalRef(value);
        env->DeleteLocalRef(header);
    }

    const char* contentType = o->contentType != nullptr ? o->contentType : DEFAULT_CONTENT_TYPE;
    return env->NewObject(
        cls, ctor,
        newString(env, o->path),
        newString(env, o->method),
        newString(env, contentType),
        list,
        static_cast<jint>(o->headerslen),
        static_cast<jboolean>(o->keepAlive));
}

jobject wsOptionsToJava(JNIEnv* env, const privmxDrvNet_WsOptions* options) {
    jclass cls = g_cls[WS_OPTIONS];
    jmethodID ctor = env->GetMethodID(cls, "<init>", "(Ljava/lang/String;)V");
    if (ctor == nullptr) return nullptr;
    return env->NewObject(cls, ctor, newString(env, options->url));
}

void releaseListener(NativeListener* l) {
    if (l->refs.fetch_sub(1, std::memory_order_acq_rel) == 1) free(l);
}

NativeListener* acquireListener(JNIEnv* env, jobject javaListener) {
    jfieldID field = env->GetFieldID(g_cls[LISTENER], "listener_ptr", "J");
    if (field == nullptr) return exceptionOccurred(env), nullptr;
    if (env->MonitorEnter(javaListener) != JNI_OK) return exceptionOccurred(env), nullptr;
    auto* l = reinterpret_cast<NativeListener*>(env->GetLongField(javaListener, field));
    if (l != nullptr) l->refs.fetch_add(1, std::memory_order_relaxed);
    env->MonitorExit(javaListener);
    return l;
}

void detachListener(JNIEnv* env, jobject javaListener) {
    jfieldID field = env->GetFieldID(g_cls[LISTENER], "listener_ptr", "J");
    if (field == nullptr) return (void)exceptionOccurred(env);
    if (env->MonitorEnter(javaListener) != JNI_OK) return (void)exceptionOccurred(env);
    env->SetLongField(javaListener, field, 0);
    env->MonitorExit(javaListener);
}

int requestInFrame(
    JNIEnv* env,
    jobject client,
    const char* data,
    int datalen,
    const privmxDrvNet_HttpRequestOptions* options,
    int* statusCode,
    char** out,
    unsigned int* outlen
) {
    jclass cls = g_cls[HTTP];
    jmethodID mid = env->GetMethodID(
        cls, "makeRequest",
        "([BL" NET "$HttpRequestOptions;)L" NET "$HttpResponse;");
    if (mid == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;

    jobject jopts = httpRequestOptionsToJava(env, options);
    if (jopts == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;
    jobject response = env->CallObjectMethod(client, mid, newByteArray(env, data, datalen), jopts);
    if (exceptionOccurred(env) || response == nullptr) return ERR_JVM_EXCEPTION;

    jclass respCls = g_cls[HTTP_RESPONSE];
    jfieldID statusField = env->GetFieldID(respCls, "statusCode", "I");
    if (statusField == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;
    jfieldID outField = env->GetFieldID(respCls, "out", "[B");
    if (outField == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;

    auto jout = static_cast<jbyteArray>(env->GetObjectField(response, outField));
    int len = 0;
    char* body = jout != nullptr ? readByteArray(env, jout, &len) : nullptr;

    *statusCode = env->GetIntField(response, statusField);
    *out = body;
    *outlen = static_cast<unsigned int>(len);
    return OK;
}
}

struct privmxDrvNet_Http {
    jobject client;
};

struct privmxDrvNet_Ws {
    jobject client;
    NativeListener* listener;

    jobject javaListener;
};

extern "C" {
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    g_vm.store(vm);
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;

    for (int i = 0; i < CLS_COUNT; ++i) {
        jclass local = env->FindClass(CLASS_NAMES[i]);
        if (local == nullptr) return JNI_ERR;
        g_cls[i] = static_cast<jclass>(env->NewGlobalRef(local));
        env->DeleteLocalRef(local);
        if (g_cls[i] == nullptr) return JNI_ERR;
    }
    return JNI_VERSION_1_6;
}

int privmxDrvNet_close() {
    JavaVM* vm = g_vm.load();
    if (vm == nullptr) return ERR_NOT_INITIALIZED;
    JNIEnv* env = nullptr;
    bool attached = vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) == JNI_OK;
    for (jclass& cls : g_cls) {
        if (attached && cls != nullptr) env->DeleteGlobalRef(cls);
        cls = nullptr;
    }
    return OK;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM*, void*) {
    privmxDrvNet_close();
    g_vm.store(nullptr);
}

int privmxDrvNet_version(unsigned int* version) {
    if (version == nullptr) return ERR_NULL_ARG;
    *version = 1;
    return OK;
}

int privmxDrvNet_setConfig(const char*) { return OK; }

int privmxDrvNet_httpCreateSession(const privmxDrv_HttpOptions* options, privmxDrvNet_Http** res) {
    if (options == nullptr || res == nullptr) return ERR_NULL_ARG;
    if (g_cls[HTTP] == nullptr) return ERR_NOT_INITIALIZED;
    if (options->baseUrl == nullptr) return ERR_NULL_ARG;

    JNIEnv* env = attach();
    if (env == nullptr) return ERR_JNI_CALL;
    LocalFrame frame(env, 4);
    if (!frame.ok) return exceptionOccurred(env), ERR_JNI_CALL;

    jobject client = newClient(env, g_cls[HTTP], options->baseUrl);
    if (client == nullptr) return exceptionOccurred(env), ERR_JNI_CALL;

    auto* http = static_cast<privmxDrvNet_Http*>(malloc(sizeof(privmxDrvNet_Http)));
    http->client = client;
    *res = http;
    return OK;
}

int privmxDrvNet_httpDestroySession(privmxDrvNet_Http* http) {
    if (http == nullptr) return ERR_NULL_ARG;
    if (http->client == nullptr) return ERR_BAD_HANDLE;
    JNIEnv* env = attach();
    if (env == nullptr) return ERR_NOT_INITIALIZED;
    env->DeleteGlobalRef(http->client);
    http->client = nullptr;
    return OK;
}

int privmxDrvNet_httpFree(privmxDrvNet_Http* http) {
    if (http == nullptr) return ERR_NULL_ARG;
    free(http);
    return OK;
}

int privmxDrvNet_httpRequest(
    privmxDrvNet_Http* http,
    const char* data,
    int datalen,
    const privmxDrvNet_HttpRequestOptions* options,
    int* statusCode,
    char** out,
    unsigned int* outlen
) {
    if (http == nullptr || options == nullptr || statusCode == nullptr || out == nullptr ||
        outlen == nullptr) {
        return ERR_NULL_ARG;
    }
    if (g_cls[HTTP] == nullptr) return ERR_NOT_INITIALIZED;
    if (http->client == nullptr) return ERR_BAD_HANDLE;

    JNIEnv* env = attach();
    if (env == nullptr) return ERR_JVM_EXCEPTION;
    LocalFrame frame(env, 16);
    if (!frame.ok) return exceptionOccurred(env), ERR_JVM_EXCEPTION;
    return requestInFrame(env, http->client, data, datalen, options, statusCode, out, outlen);
}

int privmxDrvNet_wsConnect(
    const privmxDrvNet_WsOptions* options,
    void (*onopen)(void* ctx),
    void (*onmessage)(void* ctx, const char* msg, int msglen),
    void (*onerror)(void* ctx, const char* msg, int msglen),
    void (*onclose)(void* ctx, int wasClean),
    void* ctx,
    privmxDrvNet_Ws** res
) {
    if (options == nullptr || res == nullptr) return ERR_NULL_ARG;
    jclass cls = g_cls[WS];
    if (cls == nullptr) return ERR_NOT_INITIALIZED;
    if (options->url == nullptr) return ERR_NULL_ARG;

    JNIEnv* env = attach();
    if (env == nullptr) return ERR_JNI_CALL;
    LocalFrame frame(env, 8);
    if (!frame.ok) return exceptionOccurred(env), ERR_JNI_CALL;

    jmethodID connect = env->GetMethodID(cls, "connect", "(L" NET "$WsOptions;J)V");
    if (connect == nullptr) return exceptionOccurred(env), ERR_JNI_CALL;
    jfieldID listenerField = env->GetFieldID(cls, "listener", "L" NET "$WebSocket$WebsocketListener;");
    if (listenerField == nullptr) return exceptionOccurred(env), ERR_JNI_CALL;
    jobject client = newClient(env, cls, options->url);
    if (client == nullptr) return exceptionOccurred(env), ERR_JNI_CALL;
    jobject javaListener = env->NewGlobalRef(env->GetObjectField(client, listenerField));
    if (javaListener == nullptr) {
        exceptionOccurred(env);
        env->DeleteGlobalRef(client);
        return ERR_JNI_CALL;
    }

    auto* listener = static_cast<NativeListener*>(malloc(sizeof(NativeListener)));
    listener->onOpen = onopen;
    listener->onMessage = onmessage;
    listener->onError = onerror;
    listener->onClose = onclose;
    listener->ctx = ctx;
    listener->refs.store(1, std::memory_order_relaxed);
    env->CallVoidMethod(
        client, connect, wsOptionsToJava(env, options), reinterpret_cast<jlong>(listener));
    if (exceptionOccurred(env)) {
        detachListener(env, javaListener);
        env->DeleteGlobalRef(javaListener);
        env->DeleteGlobalRef(client);
        releaseListener(listener);
        return ERR_JNI_CALL;
    }

    auto* ws = static_cast<privmxDrvNet_Ws*>(malloc(sizeof(privmxDrvNet_Ws)));
    *ws = privmxDrvNet_Ws{client, listener, javaListener};
    *res = ws;
    return OK;
}

int privmxDrvNet_wsSend(privmxDrvNet_Ws* ws, const char* data, int datalen) {
    if (ws == nullptr) return ERR_NULL_ARG;
    if (g_cls[WS] == nullptr) return ERR_NOT_INITIALIZED;
    if (ws->client == nullptr) return ERR_BAD_HANDLE;

    JNIEnv* env = attach();
    if (env == nullptr) return ERR_NOT_INITIALIZED;
    LocalFrame frame(env, 2);
    if (!frame.ok) return exceptionOccurred(env), ERR_JVM_EXCEPTION;

    jmethodID send = env->GetMethodID(g_cls[WS], "send", "([B)V");
    if (send == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;
    jbyteArray jdata = newByteArray(env, data, datalen);
    if (jdata == nullptr) return exceptionOccurred(env), ERR_JVM_EXCEPTION;
    env->CallVoidMethod(ws->client, send, jdata);
    return exceptionOccurred(env) ? ERR_JVM_EXCEPTION : OK;
}

int privmxDrvNet_wsClose(privmxDrvNet_Ws* ws) {
    if (ws == nullptr) return ERR_NULL_ARG;
    if (g_cls[WS] == nullptr) return ERR_NOT_INITIALIZED;
    if (ws->client == nullptr) return ERR_BAD_HANDLE;

    JNIEnv* env = attach();
    if (env == nullptr) return ERR_NOT_INITIALIZED;
    jmethodID disconnect = env->GetMethodID(g_cls[WS], "disconnect", "()V");
    if (disconnect != nullptr) env->CallVoidMethod(ws->client, disconnect);
    bool failed = exceptionOccurred(env);
    env->DeleteGlobalRef(ws->client);
    ws->client = nullptr;
    return failed ? ERR_JVM_EXCEPTION : OK;
}

int privmxDrvNet_wsFree(privmxDrvNet_Ws* ws) {
    if (ws == nullptr) return ERR_NULL_ARG;
    if (JNIEnv* env = attach(); env != nullptr && ws->javaListener != nullptr) {
        detachListener(env, ws->javaListener);
        env->DeleteGlobalRef(ws->javaListener);
    }
    releaseListener(ws->listener);
    free(ws);
    return OK;
}

int privmxDrvNet_freeMem(void* ptr) {
    free(ptr);
    return OK;
}

JNIEXPORT void JNICALL
Java_com_simplito_kotlin_drivers_PrivmxDrvNet_00024WebSocket_00024WebsocketListener_onOpenNative(
    JNIEnv* env, jobject thiz
) {
    NativeListener* l = acquireListener(env, thiz);
    if (l == nullptr) return;
    if (l->onOpen != nullptr) l->onOpen(l->ctx);
    releaseListener(l);
}

JNIEXPORT void JNICALL
Java_com_simplito_kotlin_drivers_PrivmxDrvNet_00024WebSocket_00024WebsocketListener_onMessageNative(
    JNIEnv* env, jobject thiz, jbyteArray message
) {
    NativeListener* l = acquireListener(env, thiz);
    if (l == nullptr) return;
    if (message != nullptr && l->onMessage != nullptr) {
        jsize len = env->GetArrayLength(message);
        jbyte* bytes = env->GetByteArrayElements(message, nullptr);
        if (bytes != nullptr) {
            l->onMessage(l->ctx, reinterpret_cast<const char*>(bytes), len);
            env->ReleaseByteArrayElements(message, bytes, JNI_ABORT);
        }
    }
    releaseListener(l);
}

JNIEXPORT void JNICALL
Java_com_simplito_kotlin_drivers_PrivmxDrvNet_00024WebSocket_00024WebsocketListener_onErrorNative(
    JNIEnv* env, jobject thiz, jstring message
) {
    NativeListener* l = acquireListener(env, thiz);
    if (l == nullptr) return;
    if (l->onError != nullptr) {
        if (message == nullptr) {
            l->onError(l->ctx, "", 0);
        } else if (const char* text = env->GetStringUTFChars(message, nullptr); text != nullptr) {
            l->onError(l->ctx, text, static_cast<int>(strlen(text)));
            env->ReleaseStringUTFChars(message, text);
        }
    }
    releaseListener(l);
}

JNIEXPORT void JNICALL
Java_com_simplito_kotlin_drivers_PrivmxDrvNet_00024WebSocket_00024WebsocketListener_onCloseNative(
    JNIEnv* env, jobject thiz, jboolean wasClean
) {
    NativeListener* l = acquireListener(env, thiz);
    if (l == nullptr) return;
    if (l->onClose != nullptr) l->onClose(l->ctx, wasClean != JNI_FALSE ? 1 : 0);
    releaseListener(l);
}
}
