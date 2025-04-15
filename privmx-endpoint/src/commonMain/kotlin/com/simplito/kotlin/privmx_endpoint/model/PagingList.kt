package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains results of listing methods.
 *
 * @param T type of items stored in list.
 * @property totalAvailable    Total items available to get
 * @property readItems         List of items read during single method call
 *
 * @category core
 * @group Core
*/
data class PagingList<T>(val totalAvailable: Long?, val readItems: List<T>?)