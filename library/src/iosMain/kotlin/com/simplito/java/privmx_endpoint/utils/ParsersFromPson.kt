package com.simplito.java.privmx_endpoint.utils

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.File
import com.simplito.java.privmx_endpoint.model.FilesConfig
import com.simplito.java.privmx_endpoint.model.Inbox
import com.simplito.java.privmx_endpoint.model.InboxEntry
import com.simplito.java.privmx_endpoint.model.InboxPublicView
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.model.Message
import com.simplito.java.privmx_endpoint.model.PagingList
import com.simplito.java.privmx_endpoint.model.ServerFileInfo
import com.simplito.java.privmx_endpoint.model.ServerMessageInfo
import com.simplito.java.privmx_endpoint.model.Store
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
    (this["policy"] as PsonObject?)?.toContainerPolicy(),
    this["messagesCount"]?.typedValue(),
    this["statusCode"]?.typedValue(),
)

internal fun PsonObject.toStore(): Store = Store(
    this["storeId"]?.typedValue(),
    this["contextId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]?.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastFileDate"]?.typedValue(),
    this["lastModifier"]?.typedValue(),
    this["users"]?.typedList()?.map { it.typedValue() },
    this["managers"]?.typedList()?.map { it.typedValue() },
    this["version"]?.typedValue(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    (this["policy"] as PsonObject?)?.toContainerPolicy(),
    this["filesCount"]?.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toInbox(): Inbox = Inbox(
    this["inboxId"]?.typedValue(),
    this["contextId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]?.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastModifier"]?.typedValue(),
    this["users"]?.typedList()?.map { it.typedValue() },
    this["managers"]?.typedList()?.map { it.typedValue() },
    this["version"]?.typedValue(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    (this["filesConfig"] as PsonObject?)?.toFilesConfig(),
    (this["policy"] as PsonObject?)?.toContainerPolicy(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toInboxPublicView(): InboxPublicView =
    InboxPublicView(
        this["inboxId"]?.typedValue(),
        this["version"]?.typedValue(),
        this["publicMeta"]?.typedValue()
    )

internal fun PsonObject.toInboxEntry(): InboxEntry = InboxEntry(
    this["entryId"]?.typedValue(),
    this["inboxId"]?.typedValue(),
    this["data"]?.typedValue(),
    this["files"]?.typedList()?.map { (it as PsonObject).toFile() },
    this["authorPubKey"]?.typedValue(),
    this["createDate"]?.typedValue(),
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
        (this["item"] as PsonObject?)?.toItemPolicy()
    )

internal fun PsonObject.toFilesConfig(): FilesConfig =
    FilesConfig(
        this["minCount"]?.typedValue(),
        this["maxCount"]?.typedValue(),
        this["maxFileSize"]?.typedValue(),
        this["maxWholeUploadSize"]?.typedValue()
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
    (this["info"] as PsonObject?)?.toServerMessageInfo(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    this["data"]?.typedValue(),
    this["authorPubKey"]?.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toFile() = File(
    (this["info"] as PsonObject?)?.toServerFileInfo(),
    this["publicMeta"]?.typedValue(),
    this["privateMeta"]?.typedValue(),
    this["size"]?.typedValue(),
    this["authorPubKey"]?.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toServerMessageInfo() = ServerMessageInfo(
    this["threadId"]?.typedValue(),
    this["messageId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]?.typedValue()
)

internal fun PsonObject.toServerFileInfo() = ServerFileInfo(
    this["storeId"]?.typedValue(),
    this["fileId"]?.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]?.typedValue()
)

internal fun <T> PsonObject.toPagingList(mapper: PsonObject.() -> T) = PagingList(
    this["totalAvailable"]?.typedValue(),
    this["readItems"]?.typedList()?.map { (it as PsonObject).mapper() }
)

@Throws(ClassCastException::class)
internal inline fun <reified T : Any> PsonValue<Any>.typedValue(): T {
    return getValue() as T
}


@Throws(ClassCastException::class)
@Suppress("UNCHECKED_CAST")
internal fun PsonValue<Any>.typedList() = getValue() as List<PsonValue<Any>>