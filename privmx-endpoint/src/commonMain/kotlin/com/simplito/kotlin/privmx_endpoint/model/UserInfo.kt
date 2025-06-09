package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains Information about user.
 *
 * @property user       User publicKey and userId.
 * @property isActive    Is user connected to bridge.
 */
class UserInfo
    (
    var user: UserWithPubKey,
    var isActive: Boolean
)