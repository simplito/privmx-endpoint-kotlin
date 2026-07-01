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

import com.simplito.kotlin.privmx_endpoint.model.events.KvdbDeletedEntryEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.KvdbStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.Kvdb
import com.simplito.kotlin.privmx_endpoint.model.KvdbEntry
import com.simplito.kotlin.privmx_endpoint.model.ServerKvdbEntryInfo
import com.simplito.kotlin.privmx_endpoint.model.BIP39
import com.simplito.kotlin.privmx_endpoint.model.BridgeIdentity
import com.simplito.kotlin.privmx_endpoint.model.CollectionItemChange
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.Context
import com.simplito.kotlin.privmx_endpoint.model.Event
import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.FileChange
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
import com.simplito.kotlin.privmx_endpoint.model.UserStatusChange
import com.simplito.kotlin.privmx_endpoint.model.UserWithAction
import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.model.stream.RecordingEncKey
import com.simplito.kotlin.privmx_endpoint.model.stream.Handle
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamPublishResult
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamRoom
import com.simplito.kotlin.privmx_endpoint.model.stream.TurnCredentials
import com.simplito.kotlin.privmx_endpoint.model.events.CollectionChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextCustomEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextUserEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ContextUsersStatusChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.InboxEntryDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreFileUpdatedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.StoreStatsChangedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadDeletedMessageEventData
import com.simplito.kotlin.privmx_endpoint.model.events.ThreadStatsEventData
import com.simplito.kotlin.privmx_endpoint.model.stream.DataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.DecryptedDataChannelMessage
import com.simplito.kotlin.privmx_endpoint.model.stream.StreamTrackInfo
import com.simplito.kotlin.privmx_endpoint.model.stream.PublishedStreamData
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

internal fun PsonObject.toUserStatusChange() = UserStatusChange(
    this["action"]!!.typedValue(),
    this["timestamp"]!!.typedValue()
)

internal fun PsonObject.toUserInfo(): UserInfo = UserInfo(
    (this["user"] as PsonObject).toUserWithPubKey(),
    this["isActive"]!!.typedValue(),
    (this["lastStatusChange"] as PsonObject?)?.toUserStatusChange()
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
    this["schemaVersion"]?.typedValue()
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
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue()
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
    (this["policy"] as PsonObject).toContainerPolicyWithoutItem(),
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue()
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
    this["schemaVersion"]?.typedValue()
)

internal fun PsonObject.toContainerPolicy(): ContainerPolicy =
    ContainerPolicy(
        this["get"]?.typedValue(),
        this["update"]?.typedValue(),
        this["delete_"]?.typedValue(),
        this["updatePolicy"]?.typedValue(),
        this["updaterCanBeRemovedFromManagers"]?.typedValue(),
        this["ownerCanBeRemovedFromManagers"]?.typedValue(),
        (this["item"] as PsonObject?)?.toItemPolicy()
    )

internal fun PsonObject.toContainerPolicyWithoutItem(): ContainerPolicyWithoutItem =
    ContainerPolicyWithoutItem(
        this["get"]?.typedValue(),
        this["update"]?.typedValue(),
        this["delete_"]?.typedValue(),
        this["updatePolicy"]?.typedValue(),
        this["updaterCanBeRemovedFromManagers"]?.typedValue(),
        this["ownerCanBeRemovedFromManagers"]?.typedValue()
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
    this["delete_"]?.typedValue()
)

internal fun PsonObject.toMessage() = Message(
    (this["info"] as PsonObject).toServerMessageInfo(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["data"]!!.typedValue(),
    this["authorPubKey"]!!.typedValue(),
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue()
)

internal fun PsonObject.toFile() = File(
    (this["info"] as PsonObject).toServerFileInfo(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["size"]?.typedValue(),
    this["authorPubKey"]!!.typedValue(),
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue(),
    this["randomWrite"]!!.typedValue()
)

private fun PsonObject.toFileChange() = FileChange(
    this["pos"]!!.typedValue(),
    this["length"]!!.typedValue(),
    this["truncate"]!!.typedValue()
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

internal fun PsonObject.toCollectionItemChange() = CollectionItemChange(
    this["itemId"]!!.typedValue(),
    this["action"]!!.typedValue(),
)

internal fun PsonObject.toUserWithAction() = UserWithAction(
    (this["user"] as PsonObject).toUserWithPubKey(),
    this["action"]!!.typedValue(),
)

internal fun <T> PsonObject.toPagingList(mapper: PsonObject.() -> T) = PagingList(
    this["totalAvailable"]?.typedValue(),
    this["readItems"]!!.typedList().map { (it as PsonObject).mapper() }
)

internal inline fun <reified T: Any> PsonObject.toValuePagingList() = PagingList(
    this["totalAvailable"]?.typedValue(),
    this["readItems"]!!.typedList().map { it.typedValue<T>() }
)

internal fun PsonObject.toEvent(): Event<*> = Event(
    this["type"]!!.typedValue(),
    this["channel"]!!.typedValue(),
    this["connectionId"]?.typedValue(),
    this["subscriptions"]!!.typedList().map { it.typedValue() },
    this["timestamp"]!!.typedValue(),
    (this["data"] as PsonObject?)?.let {
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

internal fun PsonObject.toStoreFileUpdatedEventData() = StoreFileUpdatedEventData(
    (this["file"]!! as PsonObject).toFile(),
    this["changes"]!!.typedList().map { (it as PsonObject).toFileChange() }
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

internal fun PsonObject.toContextCustomEventData() = ContextCustomEventData(
    this["contextId"]!!.typedValue(),
    this["userId"]!!.typedValue(),
    this["payload"]!!.typedValue(),
    //TODO: This will be not null
    this["statusCode"]?.typedValue() ?: 0,
    this["schemaVersion"]!!.typedValue()
)

internal fun PsonObject.toCollectionChangedEventData() = CollectionChangedEventData(
    this["moduleType"]!!.typedValue(),
    this["moduleId"]!!.typedValue(),
    this["affectedItemsCount"]!!.typedValue(),
    this["items"]!!.typedList().map { (it as PsonObject).toCollectionItemChange() }
)

internal fun PsonObject.toContextUserEventData() = ContextUserEventData(
    this["contextId"]!!.typedValue(),
    (this["user"] as PsonObject).toUserWithPubKey()
)

internal fun PsonObject.toContextUsersStatusChangedEventData() = ContextUsersStatusChangedEventData(
    this["contextId"]!!.typedValue(),
    this["users"]!!.typedList().map { (it as PsonObject).toUserWithAction() }
)

internal fun PsonObject.toKvdbDeletedEventData() = KvdbDeletedEventData(
    this["kvdbId"]!!.typedValue()
)

internal fun PsonObject.toKvdbStatsEventData() = KvdbStatsEventData(
    this["kvdbId"]!!.typedValue(),
    this["lastEntryDate"]!!.typedValue(),
    this["entries"]!!.typedValue(),
)

internal fun PsonObject.toKvdbDeletedEntryEventData() = KvdbDeletedEntryEventData(
    this["kvdbId"]!!.typedValue(),
    this["kvdbEntryKey"]!!.typedValue()

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
    "store\$StoreFileUpdatedEventData" to PsonObject::toStoreFileUpdatedEventData,
    "inbox\$InboxEntryDeletedEventData" to PsonObject::toInboxEntryDeletedEventData,
    "inbox\$InboxDeletedEventData" to PsonObject::toInboxDeletedEventData,
    "inbox\$InboxEntry" to PsonObject::toInboxEntry,
    "inbox\$Inbox" to PsonObject::toInbox,
    "inbox\$Inbox" to PsonObject::toInbox,
    "event\$ContextCustomEventData" to PsonObject::toContextCustomEventData,
    "core\$CollectionChangedEventData" to PsonObject::toCollectionChangedEventData,
    "core\$ContextUserEventData" to PsonObject::toContextUserEventData,
    "core\$ContextUsersStatusChangedEventData" to PsonObject::toContextUsersStatusChangedEventData,
    "kvdb\$Kvdb" to PsonObject::toKvdb,
    "kvdb\$KvdbDeletedEventData" to PsonObject::toKvdbDeletedEventData,
    "kvdb\$KvdbStatsEventData" to PsonObject::toKvdbStatsEventData,
    "kvdb\$KvdbEntry" to PsonObject::toKvdbEntry,
    "kvdb\$KvdbDeletedEntryEventData" to PsonObject::toKvdbDeletedEntryEventData,
)


internal fun PsonObject.toBip39(): BIP39 = BIP39(
    this["mnemonic"]!!.typedValue(),
    ExtKey(this["extKey"] as PsonValue.PsonLong),
    this["entropy"]?.typedValue()!!
)

internal fun PsonObject.toBridgeIdentity(): BridgeIdentity = BridgeIdentity(
    this["url"]!!.typedValue(),
    this["pubKey"]?.typedValue(),
    this["instanceId"]?.typedValue()
)

internal fun PsonObject.toVerificationRequest(): VerificationRequest = VerificationRequest(
    this["contextId"]!!.typedValue(),
    this["senderId"]!!.typedValue(),
    this["senderPubKey"]!!.typedValue(),
    this["date"]!!.typedValue(),
    (this["bridgeIdentity"] as PsonObject?)?.toBridgeIdentity(),
)

internal fun PsonObject.toKvdb(): Kvdb = Kvdb(
    this["contextId"]!!.typedValue(),
    this["kvdbId"]!!.typedValue(),
    this["createDate"]!!.typedValue(),
    this["creator"]!!.typedValue(),
    this["lastModificationDate"]?.typedValue(),
    this["lastModifier"]!!.typedValue(),
    this["users"]!!.typedList().map { it.typedValue() },
    this["managers"]!!.typedList().map { it.typedValue() },
    this["version"]?.typedValue(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["entries"]?.typedValue(),
    this["lastEntryDate"]?.typedValue(),
    (this["policy"] as PsonObject?)?.toContainerPolicy(),
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue(),
)

internal fun PsonObject.toKvdbEntry(): KvdbEntry = KvdbEntry(
    (this["info"] as PsonObject).toServerKvdbEntryInfo(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    this["data"]!!.typedValue(),
    //TODO: This will be not null
    this["authorPubKey"]?.typedValue() ?: "",
    this["version"]?.typedValue(),
    this["statusCode"]?.typedValue(),
    this["schemaVersion"]?.typedValue(),
)

internal fun PsonObject.toServerKvdbEntryInfo(): ServerKvdbEntryInfo = ServerKvdbEntryInfo(
    this["kvdbId"]!!.typedValue(),
    this["key"]!!.typedValue(),
    this["createDate"]?.typedValue(),
    this["author"]!!.typedValue()
)

@Throws(ClassCastException::class)
internal inline fun <reified T : Any> PsonObject.toMap(): Map<String,T> {
    return mapOf(*(getValue().map { it.key to it.value.typedValue<T>() }.toTypedArray()))
}

@Throws(ClassCastException::class)
internal inline fun <reified T : Any> PsonValue<Any>.typedValue(): T {
    return getValue() as T
}


@Throws(ClassCastException::class)
@Suppress("UNCHECKED_CAST")
internal fun PsonValue<Any>.typedList() = getValue() as List<PsonValue<Any>>

internal fun PsonObject.toTurnCredentials(): TurnCredentials = TurnCredentials(
    this["urls"]!!.typedValue(),
    this["username"]!!.typedValue(),
    this["password"]!!.typedValue(),
    this["expirationTime"]!!.typedValue()
)

internal fun PsonObject.toStreamRoom(): StreamRoom = StreamRoom(
    this["contextId"]!!.typedValue(),
    this["streamRoomId"]!!.typedValue(),
    this["createDate"]!!.typedValue(),
    this["creator"]!!.typedValue(),
    this["lastModificationDate"]!!.typedValue(),
    this["lastModifier"]!!.typedValue(),
    this["users"]!!.typedList().map { it.typedValue() },
    this["managers"]!!.typedList().map { it.typedValue() },
    this["version"]!!.typedValue(),
    this["publicMeta"]!!.typedValue(),
    this["privateMeta"]!!.typedValue(),
    (this["policy"] as PsonObject).toContainerPolicyWithoutItem(),
    //TODO: No status code in room
    0,//    this["statusCode"]!!.typedValue(),
    //TODO: No schemaVersion code in room
0,//    this["schemaVersion"]!!.typedValue(),
    //TODO: No closed info
false,//    this["closed"]!!.typedValue(),
)

internal fun PsonObject.toStreamInfo(): StreamInfo = StreamInfo(
    this["id"]!!.typedValue(),
    this["userId"]!!.typedValue(),
    this["metadata"]?.typedValue(),
    this["dummy"]?.typedValue(),
    this["tracks"]!!.typedList().map { (it as PsonObject).toStreamTrackInfo() }
)

internal fun PsonObject.toStreamTrackInfo(): StreamTrackInfo = StreamTrackInfo(
    this["type"]!!.typedValue(),
    this["mindex"]!!.typedValue(),
    this["mid"]!!.typedValue(),
    this["disabled"]?.typedValue(),
    this["codec"]?.typedValue(),
    this["description"]?.typedValue(),
    this["moderated"]?.typedValue(),
    this["simulcast"]?.typedValue()
)

internal fun PsonObject.toDecryptedDataChannelMessage(): DecryptedDataChannelMessage = DecryptedDataChannelMessage(
    this["statusCode"]!!.typedValue(),
    this["data"]!!.typedValue(),
    this["seq"]!!.typedValue()
)

internal fun PsonObject.toDataChannelMessage(): DataChannelMessage = DataChannelMessage(
    this["data"]!!.typedValue(),
    this["seq"]!!.typedValue()
)

internal fun PsonValue.PsonLong.toHandle(): Handle = this.typedValue()

internal fun PsonObject.toPublishedStream(): PublishedStreamData = PublishedStreamData(
    this["streamRoomId"]!!.typedValue(),
    (this["stream"]!! as PsonObject).toStreamInfo(),
    this["userId"]!!.typedValue()
)

//internal fun PsonObject.toStreamInfo(): StreamInfo = StreamInfo(
//    this["id"]!!.typedValue(),
//    this["userId"]!!.typedValue(),
//    this["metadata"]!!.typedValue(),
//    this["dummy"]!!.typedValue(),
//    this["userId"]!!.typedValue(),
//    this["userId"]!!.typedValue(),
//)

internal fun PsonObject.toStreamPublishResult(): StreamPublishResult = StreamPublishResult(
    this["published"]!!.typedValue(),
    (this["data"] as? PsonObject)?.toPublishedStream()
)

internal fun PsonObject.toRecordingEncKey(): RecordingEncKey = RecordingEncKey(
    this["keyId"]!!.typedValue(),
    this["key"]!!.typedValue()
)