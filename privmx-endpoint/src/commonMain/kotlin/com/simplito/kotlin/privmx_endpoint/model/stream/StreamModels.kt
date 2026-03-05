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

package com.simplito.kotlin.privmx_endpoint.model.stream

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy

/**
 * kotlin equivalent of privmx::endpoint::stream::TurnCredentials
 */
 data class TurnCredentials(
     val url: String,
     val username: String,
     val password: String,
     val expirationTime: Long
 )

data class StreamRoom(
    val contextId: String,
    val streamRoomId: String,
    val createDate: Long,
    val creator: String,
    val lastModificationDate: Long,
    val lastModifier: String,
    val users: List<String>,
    val managers: List<String>,
    val version: Long,
    val publicMeta: ByteArray,
    val privateMeta: ByteArray,
    val policy: ContainerPolicy?,
    val statusCode: Long,
    val schemaVersion: Long,
    val closed: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as StreamRoom

        if (createDate != other.createDate) return false
        if (lastModificationDate != other.lastModificationDate) return false
        if (version != other.version) return false
        if (statusCode != other.statusCode) return false
        if (schemaVersion != other.schemaVersion) return false
        if (closed != other.closed) return false
        if (contextId != other.contextId) return false
        if (streamRoomId != other.streamRoomId) return false
        if (creator != other.creator) return false
        if (lastModifier != other.lastModifier) return false
        if (users != other.users) return false
        if (managers != other.managers) return false
        if (!publicMeta.contentEquals(other.publicMeta)) return false
        if (!privateMeta.contentEquals(other.privateMeta)) return false
        if (policy != other.policy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createDate.hashCode()
        result = 31 * result + lastModificationDate.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + statusCode.hashCode()
        result = 31 * result + schemaVersion.hashCode()
        result = 31 * result + closed.hashCode()
        result = 31 * result + contextId.hashCode()
        result = 31 * result + streamRoomId.hashCode()
        result = 31 * result + creator.hashCode()
        result = 31 * result + lastModifier.hashCode()
        result = 31 * result + users.hashCode()
        result = 31 * result + managers.hashCode()
        result = 31 * result + publicMeta.contentHashCode()
        result = 31 * result + privateMeta.contentHashCode()
        result = 31 * result + (policy?.hashCode() ?: 0)
        return result
    }
}


data class Stream(
    val streamId: Long,
    val userId: String,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::SdpWithTypeModel
 */
data class SdpWithTypeModel(
    val sdp: String,
    val type: String,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::SdpWithRoomModel
 */
data class SdpWithRoomModel(
    val roomId: String,
    val sdp: String,
    val type: String,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::UpdateSessionIdModel
 */
data class UpdateSessionIdModel(
    val streamRoomId: String,
    val connectionType: String,
    val sessionId: Long,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::RoomModel
 */
data class RoomModel(
    val roomId: String,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::StreamSubscription
 */
data class StreamSubscription(
    val streamId: Long,
    val streamTrackId: String?,
)

/**
 * kotlin equivalent of privmx::endpoint::stream::StreamTrackInfo
 */
data class StreamTrackInfo(
    val type: String,
    val mindex: Long,
    val mid: String,
    val disabled: Boolean?,
    val codec: String?,
    val description: String?,
    val moderated: Boolean?,
    val simulcast: Boolean?,
    val talking: Boolean?,
)

data class StreamInfo(
    val id: Long,
    val userId: String,
    val metadata: String?,
    val dummy: Boolean?,
    val tracks: List<StreamTrackInfo>,
    val talking: Boolean?,
)

data class StreamTrackModificationPair(
    val before: StreamTrackInfo?,
    val after: StreamTrackInfo?,
)

data class StreamTrackModification(
    val streamId: Long,
    val tracks: List<StreamTrackModificationPair>,
)

data class NewStreams(
    val room: String,
    val streams: List<StreamInfo>,
)

data class PublishedStreamData(
    val streamRoomId: String,
    val stream: StreamInfo,
    val userId: String,
)

data class StreamUpdatedEventData(
    val streamRoomId: String,
    val streamsAdded: List<StreamInfo>,
    val streamsRemoved: List<StreamInfo>,
    val streamsModified: List<StreamTrackModification>,
)

data class StreamPublishResult(
    val published: Boolean,
    val data: PublishedStreamData?,
)

data class UpdatedStreamData(
    val active: Boolean,
    val type: String,
    val codec: String?,
    val streamId: Long?,
    val streamMid: String?,
    val streamDisplay: String?,
    val mindex: Long,
    val mid: String,
    val send: Boolean,
    val ready: Boolean,
)

data class StreamsUpdatedDataInternal(
    val room: String,
    val streams: List<UpdatedStreamData>,
    val jsep: SdpWithTypeModel?,
)

data class StreamsUpdatedData(
    val room: String,
    val streams: List<UpdatedStreamData>,
)

data class Settings(
    // currently no fields; reserved for future use
    val dummy: Unit? = null,
)

data class RecordingEncKey(
    val id: ByteArray,
    val key: ByteArray,
)

data class Key(
    var keyId: String,
    var key: ByteArray,
    val type: KeyType
)

enum class KeyType {
    LOCAL,
    REMOTE
}

enum class StreamEncryptionMode {
    SINGLE_KEY,
    MULTIPLE_KEY,
}

typealias StreamHandle = Long
typealias RemoteStreamId = Long
typealias RemoteTrackId = String
