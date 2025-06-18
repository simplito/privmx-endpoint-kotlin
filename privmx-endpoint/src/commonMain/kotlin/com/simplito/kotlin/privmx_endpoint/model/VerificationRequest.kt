package com.simplito.kotlin.privmx_endpoint.model

/**
 * Represents a request used for verifying a sender in a specific context.
 *
 * @property contextId    ID of the Context.
 * @property senderId     ID of the sender.
 * @property senderPubKey Public key of the sender.
 * @property date         Creation date of the data.
 * @property bridgeIdentity Bridge Identity.
*/
data class VerificationRequest(
    val contextId: String,
    val senderId: String,
    val senderPubKey: String,
    val date: Long?,
    val bridgeIdentity: BridgeIdentity? = null
)