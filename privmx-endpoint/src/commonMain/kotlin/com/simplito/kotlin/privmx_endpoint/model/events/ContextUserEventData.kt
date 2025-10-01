package com.simplito.kotlin.privmx_endpoint.model.events

import com.simplito.kotlin.privmx_endpoint.model.UserWithPubKey

/**
 * Contains information about a user who was added to or removed from the Context.
 *
 * @property contextId ID of the Context
 * @property user User
 */
class ContextUserEventData(
    val contextId: String,
    val user: UserWithPubKey,
)