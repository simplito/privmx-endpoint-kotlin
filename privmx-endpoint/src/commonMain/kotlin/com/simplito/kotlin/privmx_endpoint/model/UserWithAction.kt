package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains the user with their status change action.
 *
 * @property user User
 * @property action User status change action
 */
class UserWithAction(
    val user: UserWithPubKey,
    val action: String
)