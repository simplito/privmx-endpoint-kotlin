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

#ifndef PRIVMX_POCKET_LIB_PARSER_H
#define PRIVMX_POCKET_LIB_PARSER_H
#define RETURN_IF_EXCEPTION(ctx, val) if ((ctx)->ExceptionCheck()) return val;

#include "utils.hpp"
#include <jni.h>
#include "model_native_initializers.h"
#include "privmx/endpoint/core/Types.hpp"
#include "privmx/endpoint/core/Events.hpp"
#include "privmx/endpoint/inbox/Types.hpp"
#include "privmx/endpoint/stream/Events.hpp"

std::vector<privmx::endpoint::core::UserWithPubKey>
usersToVector(JniContextUtils &ctx, jobjectArray users);

privmx::endpoint::core::PKIVerificationOptions
parsePKIVerificationOptions(JniContextUtils &ctx, jobject pkiVerificationOptions);

privmx::endpoint::core::ContainerPolicyWithoutItem
parseContainerPolicyWithoutItem(JniContextUtils &ctx, jobject containerPolicyWithoutItem);

privmx::endpoint::core::ContainerPolicy
parseContainerPolicy(JniContextUtils &ctx, jobject containerPolicy);

privmx::endpoint::core::ItemPolicy parseItemPolicy(JniContextUtils &ctx, jobject itemPolicy);

privmx::endpoint::inbox::FilesConfig parseFilesConfig(JniContextUtils &ctx, jobject filesConfig);

// streams
privmx::endpoint::stream::StreamSubscription parseStreamSubscription(JniContextUtils &ctx, jobject streamSubscription);

privmx::endpoint::stream::SdpWithTypeModel parseSdpWithTypeModel(JniContextUtils &ctx, jobject sdpWithTypeModel);


jobject parseEvent(JniContextUtils &ctx, std::shared_ptr<privmx::endpoint::core::Event> event);

privmx::endpoint::core::PagingQuery parsePagingQuery(JniContextUtils &ctx, jobject pagingQuery);

privmx::endpoint::stream::DataChannelMessage parseDataChannelMessage(JniContextUtils &ctx, jobject dataChannelMessage);

// java -> c++

int64_t jobject2long(JniContextUtils &ctx, jobject jLong);
std::string jobject2string(JniContextUtils &ctx, jobject jString);

template<typename T>
std::vector<T> jArrayToVector(
        JniContextUtils &ctx,
        jobjectArray jArray,
        std::function<T(JniContextUtils &, jobject)> fun,
        bool requireNonNulls
) {
    std::vector<T> result;

    for (int i = 0; i < ctx->GetArrayLength(jArray); i++) {
        jobject element = ctx->GetObjectArrayElement(jArray, i);

        if (requireNonNulls) {
            if (ctx.nullCheck(element, "Array element")) {
                return{};
            }
        }

        result.push_back(fun(ctx, element));
    }

    return result;
}

template<typename T>
std::vector<T> jArrayToVector(
        JniContextUtils &ctx,
        jobjectArray jArray,
        std::function<T(JniContextUtils &, jobject)> fun
) {
    return jArrayToVector(ctx, jArray, fun, false);
}


// c++ -> java

jobject string2jobject(JniContextUtils &ctx, const std::string &cstring);
jobject long2jobject(JniContextUtils &ctx, const int64_t &clong);

template<typename T, typename F>
jobject vectorTojArray(
        JniContextUtils &ctx,
        const std::vector<T> &vector,
        F fun,
        bool requireNonNulls
) {
    jclass arrayListCls = ctx->FindClass("java/util/ArrayList");
    jmethodID initMID = ctx->GetMethodID(arrayListCls, "<init>", "()V");
    jmethodID addToListMID = ctx->GetMethodID(arrayListCls, "add", "(Ljava/lang/Object;)Z");

    jobject listObj = ctx->NewObject(arrayListCls, initMID);

    for (const auto &item: vector) {
        jobject jItem = fun(ctx, item);
        if (requireNonNulls) {
            if (ctx.nullCheck(jItem, "Array element")) {return nullptr;}
        }

        ctx->CallBooleanMethod(listObj, addToListMID, jItem);
    }
    return listObj;
}

template<typename T, typename F>
jobject vectorTojArray(JniContextUtils &ctx, const std::vector<T> &vector, F fun) {
    return vectorTojArray(ctx, vector, fun, false);
}

template<typename T, typename F>
jobject pagingList2Java(
        JniContextUtils &ctx,
        privmx::endpoint::core::PagingList<T> pagingList,
        F fun
) {
    jclass pagingListCls = ctx->FindClass(
            "com/simplito/kotlin/privmx_endpoint/model/PagingList");
    jmethodID pagingListInitMID = ctx->GetMethodID(pagingListCls, "<init>",
            "(Ljava/lang/Long;Ljava/util/List;)V"
    );

    jobject array = vectorTojArray(ctx, pagingList.readItems, fun);

    return ctx->NewObject(
            pagingListCls,
            pagingListInitMID,
            ctx.long2jLong(pagingList.totalAvailable),
            array
    );
}

#endif //PRIVMX_POCKET_LIB_PARSER_H
