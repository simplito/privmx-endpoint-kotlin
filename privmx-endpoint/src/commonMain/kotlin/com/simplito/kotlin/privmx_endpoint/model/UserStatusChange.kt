package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains information about the change of user status.
 *
 * @property action Timestamp of the change
 * @property timestamp User status change action, which can be "login" or "logout".
 */
class UserStatusChange(
    val action: String,
    val timestamp: Long
)