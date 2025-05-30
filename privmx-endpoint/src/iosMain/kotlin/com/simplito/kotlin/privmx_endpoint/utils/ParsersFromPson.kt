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

import com.simplito.java.privmx_endpoint.model.BIP39
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.FilesConfig
import com.simplito.kotlin.privmx_endpoint.model.Inbox
import com.simplito.kotlin.privmx_endpoint.model.InboxEntry
import com.simplito.kotlin.privmx_endpoint.model.InboxPublicView
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint.model.Message
import com.simplito.kotlin.privmx_endpoint.model.PagingList
import com.simplito.kotlin.privmx_endpoint.model.ServerFileInfo
import com.simplito.kotlin.privmx_endpoint.model.ServerMessageInfo
import com.simplito.kotlin.privmx_endpoint.model.Store
import com.simplito.kotlin.privmx_endpoint.model.Thread
import com.simplito.kotlin.privmx_endpoint.model.UserInfo
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxEntryDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData
import com.simplito.kotlin.privmx_endpoint.modules.crypto.ExtKey
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue.PsonObject

internal fun PsonObject.toContext(): Context = Context(
    this["userId"]!!.typedValue(),
    this["contextId"]!!.typedValue()
)

internal fun PsonObject.toUserWithPubKey(): UserWithPubKey = UserWithPubKey(
    this["userId"]!!.typedValue(),
    this["pubKey"]!!.typedValue()
)

internal fun PsonObject.toUserInfo(): UserInfo = UserInfo(
    (this["user"] as PsonObject).toUserWithPubKey(),
    this["isActive"]!!.typedValue()
)

internal fun PsonObject.toThread(): Thread = Thread(
    this["contextId"]!!.typedValue(),
    this["threadId"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]!!.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastModifier"]!!.typedValue(),
    this["users"]!!.typedList().map { it.typedValue() },
    this["managers"]!!.typedList().map { it.typedValue() },
    this["version"]?.typedValue(),
    this["lastMsgDate"]?.typedValue(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    (this["policy"] as PsonObject).toContainerPolicy(),
    this["messagesCount"]?.typedValue(),
    this["statusCode"]?.typedValue(),
)

internal fun PsonObject.toStore(): Store = Store(
    this["storeId"]!!.typedValue(),
    this["contextId"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]!!.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastFileDate"]?.typedValue(),
    this["lastModifier"]!!.typedValue(),
    this["users"]!!.typedList().map { it.typedValue() },
    this["managers"]!!.typedList().map { it.typedValue() },
    this["version"]?.typedValue(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    (this["policy"] as PsonObject).toContainerPolicy(),
    this["filesCount"]?.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toInbox(): Inbox = Inbox(
    this["inboxId"]!!.typedValue(),
    this["contextId"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["creator"]!!.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastModifier"]!!.typedValue(),
    this["users"]!!.typedList().map { it.typedValue() },
    this["managers"]!!.typedList().map { it.typedValue() },
    this["version"]?.typedValue(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    (this["filesConfig"] as PsonObject?)?.toFilesConfig(),
    (this["policy"] as PsonObject).toContainerPolicy(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toInboxPublicView(): InboxPublicView =
    InboxPublicView(
        this["inboxId"]!!.typedValue(),
        this["version"]?.typedValue(),
        this["publicMeta"]!!.typedValue()
    )

internal fun PsonObject.toInboxEntry(): InboxEntry = InboxEntry(
    this["entryId"]!!.typedValue(),
    this["inboxId"]!!.typedValue(),
    this["data"]!!.typedValue(),
    this["files"]!!.typedList().map { (it as PsonObject).toFile() },
    this["authorPubKey"]!!.typedValue(),
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
    (this["info"] as PsonObject).toServerMessageInfo(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["data"]!!.typedValue(),
    this["authorPubKey"]!!.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toFile() = File(
    (this["info"] as PsonObject).toServerFileInfo(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["size"]?.typedValue(),
    this["authorPubKey"]!!.typedValue(),
    this["statusCode"]?.typedValue()
)

internal fun PsonObject.toServerMessageInfo() = ServerMessageInfo(
    this["threadId"]!!.typedValue(),
    this["messageId"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]!!.typedValue()
)

internal fun PsonObject.toServerFileInfo() = ServerFileInfo(
    this["storeId"]!!.typedValue(),
    this["fileId"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]!!.typedValue()
)

internal fun <T> PsonObject.toPagingList(mapper: PsonObject.() -> T) = PagingList(
    this["totalAvailable"]?.typedValue(),
    this["readItems"]!!.typedList().map { (it as PsonObject).mapper() }
)

internal fun PsonObject.toEvent(): Event<*> = Event(
    this["type"]!!.typedValue(),
    this["channel"]!!.typedValue(),
    this["connectionId"]?.typedValue(),
    (this["data"] as PsonObject?)?.let {
        println(it.type)
        EventDataMappers[it.type]?.invoke(it)
    } ?: Unit
)

internal fun PsonObject.toInboxDeletedEventData() = InboxDeletedEventData(
    this["inboxId"]!!.typedValue()
)

internal fun PsonObject.toInboxEntryDeletedEventData() = InboxEntryDeletedEventData(
    this["inboxId"]!!.typedValue(),
    this["entryId"]!!.typedValue()
)

internal fun PsonObject.toStoreDeletedEventData() = StoreDeletedEventData(
    this["storeId"]!!.typedValue(),
)

internal fun PsonObject.toStoreFileDeletedEventData() = StoreFileDeletedEventData(
    this["fileId"]!!.typedValue(),
    this["contextId"]!!.typedValue(),
    this["storeId"]!!.typedValue(),
)

internal fun PsonObject.toStoreStatsChangedEventData() = StoreStatsChangedEventData(
    this["storeId"]!!.typedValue(),
    this["contextId"]!!.typedValue(),
    this["lastFileDate"]?.typedValue(),
    this["filesCount"]?.typedValue(),
)

internal fun PsonObject.toThreadDeletedEventData() = ThreadDeletedEventData(
    this["threadId"]!!.typedValue()
)

internal fun PsonObject.toThreadDeletedMessageEventData() = ThreadDeletedMessageEventData(
    this["threadId"]!!.typedValue(),
    this["messageId"]!!.typedValue(),
)

internal fun PsonObject.toThreadStatsEventData() = ThreadStatsEventData(
    this["threadId"]!!.typedValue(),
    this["lastMsgDate"]?.typedValue(),
    this["messagesCount"]?.typedValue(),
)

private val EventDataMappers: Map<String, PsonObject.() -> Any> = mapOf(
    "thread\$Thread" to PsonObject::toThread,
    "thread\$Thread" to PsonObject::toThread,
    "thread\$ThreadDeletedEventData" to PsonObject::toThreadDeletedEventData,
    "thread\$ThreadStatsEventData" to PsonObject::toThreadStatsEventData,
    "thread\$Message" to PsonObject::toMessage,
    "thread\$Message" to PsonObject::toMessage,
    "thread\$ThreadDeletedMessageEventData" to PsonObject::toThreadDeletedMessageEventData,
    "store\$Store" to PsonObject::toStore,
    "store\$Store" to PsonObject::toStore,
    "store\$StoreDeletedEventData" to PsonObject::toStoreDeletedEventData,
    "store\$StoreStatsChangedEventData" to PsonObject::toStoreStatsChangedEventData,
    "store\$File" to PsonObject::toFile,
    "store\$File" to PsonObject::toFile,
    "store\$StoreFileDeletedEventData" to PsonObject::toStoreFileDeletedEventData,
    "inbox\$InboxEntryDeletedEventData" to PsonObject::toInboxEntryDeletedEventData,
    "inbox\$InboxDeletedEventData" to PsonObject::toInboxDeletedEventData,
    "inbox\$InboxEntry" to PsonObject::toInboxEntry,
    "inbox\$Inbox" to PsonObject::toInbox,
    "inbox\$Inbox" to PsonObject::toInbox,
)

internal fun PsonObject.toExtKey(): ExtKey = ExtKey()

internal fun PsonObject.toBip39(): BIP39 = BIP39(
    this["mnemonic"]!!.typedValue(),
    (this["extKey"] as PsonObject?)?.toExtKey()!!,
    this["entropy"]?.typedValue()!!
)

@Throws(ClassCastException::class)
internal inline fun <reified T : Any> PsonValue<Any>.typedValue(): T {
    return getValue() as T
}


@Throws(ClassCastException::class)
@Suppress("UNCHECKED_CAST")
internal fun PsonValue<Any>.typedList() = getValue() as List<PsonValue<Any>>