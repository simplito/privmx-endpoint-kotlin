package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains base Context information.
 *
 * @property userId        ID of the user requesting information.
 * @property contextId     ID of the Context.
 *
 * @category core
 * @group Core
 */
data class Context(val userId: String?, val contextId: String?)