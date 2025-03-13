package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains results of listing methods.
 *
 * @param <T> type of items stored in list.
 * @category core
 * @group Core
</T> */
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