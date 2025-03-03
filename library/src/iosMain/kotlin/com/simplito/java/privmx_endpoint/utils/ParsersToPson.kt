package com.simplito.java.privmx_endpoint.utils

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.modules.thread.mapOfWithNulls
import kotlin.ByteArray

internal val ItemPolicy.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "get" to get.nullablePson,
        "listMy" to listMy.nullablePson,
        "listAll" to listAll.nullablePson,
        "create" to create.nullablePson,
        "update" to update.nullablePson,
        "delete" to delete.nullablePson,
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
        "delete" to delete.nullablePson,
        "updatePolicy" to updatePolicy.nullablePson,
        "updaterCanBeRemovedFromManagers" to updaterCanBeRemovedFromManagers.nullablePson,
        "ownerCanBeRemovedFromManagers" to ownerCanBeRemovedFromManagers.nullablePson,
        "item" to (item?.pson ?: KPSON_NULL)
    ).pson


internal val String.pson: PsonValue.PsonString
    get() = PsonValue.PsonString(this)

internal val String?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonString(this)} ?: KPSON_NULL

internal val Boolean.pson: PsonValue.PsonBoolean
    get() = PsonValue.PsonBoolean(this)

internal val Boolean?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonBoolean(this)} ?: KPSON_NULL

internal val ByteArray.pson: PsonValue.PsonBinary
    get() = PsonValue.PsonBinary(this)

internal val ByteArray?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonBinary(this)} ?: KPSON_NULL

internal val Long.pson: PsonValue.PsonLong
    get() = PsonValue.PsonLong(this)

internal val Long?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonLong(this)} ?: KPSON_NULL

internal val Int.pson: PsonValue.PsonInt
    get() = PsonValue.PsonInt(this)

internal val Int?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonInt(this)} ?: KPSON_NULL

internal val Float.pson: PsonValue.PsonFloat
    get() = PsonValue.PsonFloat(this)

internal val Float?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonFloat(this)} ?: KPSON_NULL

internal val Double.pson: PsonValue.PsonDouble
    get() = PsonValue.PsonDouble(this)

internal val Double?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonDouble(this)} ?: KPSON_NULL


internal val List<PsonValue<Any>>.pson: PsonValue.PsonArray<PsonValue<Any>>
    get() = PsonValue.PsonArray(this)

internal val List<PsonValue<Any>>?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonArray(this)} ?: KPSON_NULL

internal val Map<String, PsonValue<Any>>.pson: PsonValue.PsonObject
    get() = PsonValue.PsonObject(this)

internal val Map<String, PsonValue<Any>>?.nullablePson: PsonValue<Any>
    get() = this?.let { PsonValue.PsonObject(this)} ?: KPSON_NULL
