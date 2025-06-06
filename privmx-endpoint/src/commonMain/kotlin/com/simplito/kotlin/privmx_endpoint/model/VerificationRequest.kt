package com.simplito.kotlin.privmx_endpoint.model

/**
 * Represents a request used for verifying a sender in a specific context.
 *
 * @param contextId    ID of the Context
 * @param senderId     ID of the sender
 * @param senderPubKey Public key of the sender
 * @param date         Creation date of the data
 * @param bridgeIdentity Bridge Identity
*/
class VerificationRequest
(
    var contextId: String,
    var senderId: String,
    var senderPubKey: String,
    var date: Long?,
    var bridgeIdentity: BridgeIdentity? = null
)
