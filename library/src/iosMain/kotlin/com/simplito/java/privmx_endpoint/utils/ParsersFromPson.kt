package com.simplito.java.privmx_endpoint.utils

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.model.Message
import com.simplito.java.privmx_endpoint.model.ServerMessageInfo
import com.simplito.java.privmx_endpoint.model.Thread
import com.simplito.java.privmx_endpoint.utils.PsonValue.PsonObject


internal fun PsonObject.toThread(): Thread = Thread(
    this["contextId"]?.typedValue(),
    this["threadId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]?.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastModifier"]?.typedValue(),
    this["users"]?.typedList()?.map { it.typedValue() },
    this["managers"]?.typedList()?.map { it.typedValue() },
    this["version"]?.typedValue(),
    this["lastMsgDate"]?.typedValue(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    this["policy"]?.typedObject()?.toContainerPolicy(),
    this["messagesCount"]?.typedValue(),
    this["statusCode"]?.typedValue(),
)

internal fun PsonObject.toContainerPolicy(): ContainerPolicy =
    ContainerPolicy(
        this["get"]?.typedValue(),
        this["update"]?.typedValue(),
        this["delete"]?.typedValue(),
        this["updatePolicy"]?.typedValue(),
        this["updaterCanBeRemovedFromManagers"]?.typedValue(),
        this["ownerCanBeRemovedFromManagers"]?.typedValue(),
        this["item"]?.typedObject()?.toItemPolicy()
    )

internal fun PsonObject.toItemPolicy(): ItemPolicy = ItemPolicy(
    this["get"]?.typedValue(),
    this["listMy"]?.typedValue(),
    this["listAll"]?.typedValue(),
    this["create"]?.typedValue(),
    this["update"]?.typedValue(),
    this["delete"]?.typedValue()
)

internal fun PsonObject.toMessage() = Message(
    this["info"]?.typedObject()?.toServerMessageInfo(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    this["data"]?.typedValue(),
    this["authorPubKey"]?.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toServerMessageInfo() = ServerMessageInfo(
    this["threadId"]?.typedValue(),
    this["messageId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]?.typedValue()
)

@Throws(ClassCastException::class)
internal inline fun <reified T : Any> PsonValue<Any>.typedValue(): T {
    return getValue() as T
}


@Throws(ClassCastException::class)
@Suppress("UNCHECKED_CAST")
internal fun PsonValue<Any>.typedList() = getValue() as List<PsonValue<Any>>

@Throws(ClassCastException::class)
@Suppress("UNCHECKED_CAST")
internal fun PsonValue<Any>.typedObject() = getValue() as PsonObject



