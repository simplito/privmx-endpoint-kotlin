package com.simplito.kotlin.privmx_endpoint.model.events

/**
 * Holds information about emitted custom event.
 *
 * @property contextId id of inbox from which it was sent
 * @property userId id of user which sent it
 * @property data event data
 */
class ContextCustomEventData(
    val contextId: String,
    val userId: String,
    val data: ByteArray
)
