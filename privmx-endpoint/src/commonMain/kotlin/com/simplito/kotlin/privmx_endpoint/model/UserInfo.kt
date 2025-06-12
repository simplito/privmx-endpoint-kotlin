package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains Information about user.
 *
 * @property user       User publicKey and userId.
 * @property isActive    Is user connected to bridge.
 */
data class UserInfo(
    val user: UserWithPubKey,
    val isActive: Boolean
)