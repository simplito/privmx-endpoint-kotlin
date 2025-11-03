#include <jni.h>
#include <privmx/endpoint/stream/StreamApi.hpp>
#include "../utils.hpp"
#include "../model_native_initializers.h"
#include "Connection.h"
#include "EventApi.h"

using namespace privmx::endpoint;

#ifndef PRIVMXENDPOINT_STREAMAPI_H
#define PRIVMXENDPOINT_STREAMAPI_H

privmx::endpoint::stream::StreamApi *getStreamApi(JniContextUtils &ctx, jobject streamApiInstance);

#endif //PRIVMXENDPOINT_STREAMAPI_H
