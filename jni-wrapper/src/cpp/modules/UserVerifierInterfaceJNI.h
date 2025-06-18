//
// Created by Dominika on 27/03/2025.
//

#include <jni.h>
#include "privmx/endpoint/core/UserVerifierInterface.hpp"

#ifndef PRIVMXENDPOINT_USERVERIFIERINTERFACEJNI_H
#define PRIVMXENDPOINT_USERVERIFIERINTERFACEJNI_H

namespace privmx::wrapper {
    class UserVerifierInterfaceJNI : public endpoint::core::UserVerifierInterface {
    public:
        UserVerifierInterfaceJNI(JNIEnv *env, jobject juserVerifierInterface);

        ~UserVerifierInterfaceJNI() override;

        std::vector<bool>
        verify(const std::vector<endpoint::core::VerificationRequest> &request) override;

        UserVerifierInterfaceJNI(const UserVerifierInterfaceJNI &) = delete;

        UserVerifierInterfaceJNI &operator=(const UserVerifierInterfaceJNI &) = delete;

        UserVerifierInterfaceJNI() = delete;

    private:
        jobject juserVerifierInterface;
        JavaVM *javaVM;
    };
};

#endif //PRIVMXENDPOINT_USERVERIFIERINTERFACEJNI_H