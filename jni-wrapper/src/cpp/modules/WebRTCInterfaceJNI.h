//
// Created by Dawid Jenczewski on 13/02/2025.
//

#ifndef PRIVMXENDPOINT_WEBRTCINTERFACEJNI_H
#define PRIVMXENDPOINT_WEBRTCINTERFACEJNI_H

#include "jni.h"
#include <privmx/endpoint/stream/WebRTCInterface.hpp>

using namespace privmx::endpoint::stream;

class WebRTCInterfaceJNI : public WebRTCInterface {
public:
    WebRTCInterfaceJNI(JNIEnv *env, jobject jwebRTCInterface);

//    std::string createOfferAndSetLocalDescription() override;
//
//    std::string
//    createAnswerAndSetDescriptions(const std::string &sdp, const std::string &type) override;
//
//    void setAnswerAndSetRemoteDescription(const std::string &sdp, const std::string &type) override;
//
//    void close() override;
//
//    void updateKeys(const std::vector<Key> &keys) override;

    std::string createOfferAndSetLocalDescription(const std::string &streamRoomId) override;

    std::string createAnswerAndSetDescriptions(
            const std::string &streamRoomId,
            const std::string &sdp,
            const std::string &type
    ) override;

    void setAnswerAndSetRemoteDescription(
            const std::string &streamRoomId,
            const std::string &sdp,
            const std::string &type
    ) override;

    void updateSessionId(
            const std::string &streamRoomId,
            const int64_t sessionId,
            const std::string &connectionType
    ) override;

    void close(const std::string &streamRoomId) override;

    void updateKeys(
            const std::string &streamRoomId,
            const std::vector<Key> &keys
    ) override;

    WebRTCInterfaceJNI (const WebRTCInterfaceJNI&) = delete;
    WebRTCInterfaceJNI& operator= (const WebRTCInterfaceJNI&) = delete;
//protected:
    ~WebRTCInterfaceJNI() override = default;

private:
    jobject jwebRTCInterface;
    JavaVM *javaVM;

    JNIEnv *AttachCurrentThreadIfNeeded();
};


#endif //PRIVMXENDPOINT_WEBRTCINTERFACEJNI_H
