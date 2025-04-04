package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains results of listing methods.
 *
 * @param T type of items stored in list.
 * @param totalAvailable    Total items available to get
 * @param readItems         List of items read during single method call
 *
 * @category core
 * @group Core
*/
data class PagingList<T>(
    /**
     * Total items available to get.
     */
    val totalAvailable: Long?,
    /**
     * List of items read during single method call.
     */
    val readItems: List<T>?
)