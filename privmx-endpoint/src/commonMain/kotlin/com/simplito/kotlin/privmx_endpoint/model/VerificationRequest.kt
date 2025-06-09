package com.simplito.kotlin.privmx_endpoint.model

/**
 *
 * @property contextId    ID of the Context.
 * @property senderId     ID of the sender.
 * @property senderPubKey Public key of the sender.
 * @property date         Creation date of the data.
 * @property bridgeIdentity Bridge Identity.
*/
class VerificationRequest
(
    var contextId: String,
    var senderId: String,
    var senderPubKey: String,
    var date: Long?,
    var bridgeIdentity: BridgeIdentity? = null
)