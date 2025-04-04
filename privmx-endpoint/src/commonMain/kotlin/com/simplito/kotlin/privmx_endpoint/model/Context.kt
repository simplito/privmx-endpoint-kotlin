package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains base Context information.
 *
 * @param userId        ID of the user requesting information.
 * @param contextId     ID of the Context.
 *
 * @category core
 * @group Core
 */
data class Context(
    /**
     * ID of the user requesting information.
     */
    val userId: String?,
    /**
     * ID of the Context.
     */
    val contextId: String?
)