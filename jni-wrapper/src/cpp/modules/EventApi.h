#include <jni.h>
#include "../utils.hpp"
#include "Connection.h"
#include "../parser.h"
#include "privmx/endpoint/event/EventApi.hpp"

using namespace privmx::endpoint;

#ifndef PRIVMXENDPOINT_EVENTAPI_H
#define PRIVMXENDPOINT_EVENTAPI_H

#endif //PRIVMXENDPOINT_EVENTAPI_H
privmx::endpoint::event::EventApi *getEventApi(JNIEnv *env, jobject eventApiInstance);
