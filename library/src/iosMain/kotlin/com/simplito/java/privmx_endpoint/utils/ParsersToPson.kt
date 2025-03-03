package com.simplito.java.privmx_endpoint.utils

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.model.UserWithPubKey
import com.simplito.java.privmx_endpoint.modules.thread.mapOfWithNulls

internal val ItemPolicy.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        get?.let { "get" to get.pson},
        listMy?.let { "listMy" to listMy.pson },
        listAll?.let { "listAll" to listAll.pson },
        create?.let { "create" to create.pson },
        update?.let { "update" to update.pson },
        delete?.let { "delete" to delete.pson },
    ).pson


internal val UserWithPubKey.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        "userId" to userId!!.pson,
        "pubKey" to pubKey!!.pson,
    ).pson

internal val ContainerPolicy.pson: PsonValue.PsonObject
    get() = mapOfWithNulls(
        get?.let { "get" to get.pson },
        update?.let { "update" to update.pson },
        delete?.let { "delete" to delete.pson },
        updatePolicy?.let { "updatePolicy" to updatePolicy.pson },
        updaterCanBeRemovedFromManagers?.let {
            "updaterCanBeRemovedFromManagers" to updaterCanBeRemovedFromManagers.pson
        },
        ownerCanBeRemovedFromManagers?.let {
            "ownerCanBeRemovedFromManagers" to ownerCanBeRemovedFromManagers.pson
        },
        item?.let { "item" to item.pson }
    ).pson


internal val String.pson: PsonValue.PsonString
    get() = PsonValue.PsonString(this)

internal val Boolean.pson: PsonValue.PsonBoolean
    get() = PsonValue.PsonBoolean(this)

internal val ByteArray.pson: PsonValue.PsonBinary
    get() = PsonValue.PsonBinary(this)

internal val Long.pson: PsonValue.PsonLong
    get() = PsonValue.PsonLong(this)

internal val Int.pson: PsonValue.PsonInt
    get() = PsonValue.PsonInt(this)

internal val Float.pson: PsonValue.PsonFloat
    get() = PsonValue.PsonFloat(this)

internal val Double.pson: PsonValue.PsonDouble
    get() = PsonValue.PsonDouble(this)


internal val List<PsonValue<Any>>.pson: PsonValue.PsonArray<PsonValue<Any>>
    get() = PsonValue.PsonArray(this)

internal val Map<String, PsonValue<Any>>.pson: PsonValue.PsonObject
    get() = PsonValue.PsonObject(this)
