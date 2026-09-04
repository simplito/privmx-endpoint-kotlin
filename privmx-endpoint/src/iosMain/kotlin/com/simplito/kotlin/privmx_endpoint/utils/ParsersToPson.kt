//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint.utils

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.Document
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.IndexMode
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint.model.LockLevel
import com.simplito.kotlin.privmx_endpoint.model.PKIVerificationOptions
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.stream.DataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.SdpWithTypeModel
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamSubscription

internal val ItemPolicy.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "get" to get.nullablePson,
        "listMy" to listMy.nullablePson,
        "listAll" to listAll.nullablePson,
        "create" to create.nullablePson,
        "update" to update.nullablePson,
        "delete_" to delete.nullablePson,
    ).pson


internal val UserWithPubKey.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "userId" to userId!!.pson,
        "pubKey" to pubKey!!.pson,
    ).pson

internal val ContainerPolicy.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "get" to get.nullablePson,
        "update" to update.nullablePson,
        "delete_" to delete.nullablePson,
        "updatePolicy" to updatePolicy.nullablePson,
        "updaterCanBeRemovedFromManagers" to updaterCanBeRemovedFromManagers.nullablePson,
        "ownerCanBeRemovedFromManagers" to ownerCanBeRemovedFromManagers.nullablePson,
        "item" to (item?.pson ?: KPSON_NULL)
    ).pson

internal val ContainerPolicyWithoutItem.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "get" to get.nullablePson,
        "update" to update.nullablePson,
        "delete_" to delete.nullablePson,
        "updatePolicy" to updatePolicy.nullablePson,
        "updaterCanBeRemovedFromManagers" to updaterCanBeRemovedFromManagers.nullablePson,
        "ownerCanBeRemovedFromManagers" to ownerCanBeRemovedFromManagers.nullablePson,
    ).pson

internal val FilesConfig.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "minCount" to minCount.nullablePson,
        "maxCount" to maxCount.nullablePson,
        "maxFileSize" to maxFileSize.nullablePson,
        "maxWholeUploadSize" to maxWholeUploadSize.nullablePson,
    ).pson

internal val Document.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "documentId" to documentId.pson,
        "name" to name.pson,
        "content" to content.pson,
    ).pson

internal val IndexMode.pson: PsonValue.PsonLong
    get() = PsonValue.PsonLong(ordinal.toLong())

internal val LockLevel.pson: PsonValue.PsonLong
    get() = PsonValue.PsonLong(ordinal.toLong())

internal val PKIVerificationOptions.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "bridgePubKey" to bridgePubKey.nullablePson,
        "bridgeInstanceId" to bridgeInstanceId.nullablePson
    ).pson

internal val DataChannelMessage.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "data" to data.pson,
        "seq" to seq.nullablePson,
    ).pson

internal val String.pson: PsonValue.PsonString
    get() = PsonValue.PsonString(this)

internal val String?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonString(this) } ?: KPSON_NULL

internal val Boolean.pson: PsonValue.PsonBoolean
    get() = PsonValue.PsonBoolean(this)

internal val Boolean?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonBoolean(this) } ?: KPSON_NULL

internal val ByteArray.pson: PsonValue.PsonBinary
    get() = PsonValue.PsonBinary(this)

internal val ByteArray?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonBinary(this) } ?: KPSON_NULL

internal val Long.pson: PsonValue.PsonLong
    get() = PsonValue.PsonLong(this)

internal val Long?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonLong(this) } ?: KPSON_NULL

internal val Int.pson: PsonValue.PsonInt
    get() = PsonValue.PsonInt(this)

internal val Int?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonInt(this) } ?: KPSON_NULL

internal val Float.pson: PsonValue.PsonFloat
    get() = PsonValue.PsonFloat(this)

internal val Float?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonFloat(this) } ?: KPSON_NULL

internal val Double.pson: PsonValue.PsonDouble
    get() = PsonValue.PsonDouble(this)

internal val Double?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonDouble(this) } ?: KPSON_NULL


internal val List<PsonValue<Any>>.pson: PsonValue.PsonArray<PsonValue<Any>>
    get() = PsonValue.PsonArray(this)

internal val List<PsonValue<Any>>?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonArray(this) } ?: KPSON_NULL

internal val Map<String, PsonValue<Any>>.pson: PsonValue.PsonObject
    get() = PsonValue.PsonObject(this)

internal val Map<String, PsonValue<Any>>?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonObject(this) } ?: KPSON_NULL


internal val StreamSubscription.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "streamId" to streamId!!.pson,
        "streamTrackId" to streamTrackId.nullablePson
    ).pson

internal val SdpWithTypeModel.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "sdp" to sdp.pson,
        "type" to type.pson
    ).pson
