package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds a custom notification that another member of the Group sent.
 *
 * The payload arrives sealed with the Group's own key and is opened here, so nothing about the
 * notification's cost depends on how many members the Group has — the sender sealed it once and the
 * bridge relayed it once.
 *
 * @property groupId Group ID
 * @property channelName name of the channel the notification was sent on
 * @property userId ID of the sender, as the bridge reported it. NOT authenticated — see [authorPubKey].
 * @property authorPubKey public key of the sender (base58-DER encoded), whose signature over the
 *           payload has been verified. This is the field that attests to the author. EMPTY when
 *           [statusCode] is non-zero.
 * @property payload decrypted payload. EMPTY when [statusCode] is non-zero.
 * @property statusCode 0 when the payload was opened. Otherwise, the error that stopped it — the
 *           Group's key for this notification could not be resolved, or the payload did not verify.
 */
data class GroupCustomEventData(
    val groupId: String,
    val channelName: String,
    val userId: String,
    val authorPubKey: String,
    val payload: ByteArray,
    val statusCode: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GroupCustomEventData

        if (statusCode != other.statusCode) return false
        if (groupId != other.groupId) return false
        if (channelName != other.channelName) return false
        if (userId != other.userId) return false
        if (authorPubKey != other.authorPubKey) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + channelName.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + authorPubKey.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}