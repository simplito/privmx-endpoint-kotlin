package com.simplito.kotlin.privmx_endpoint.model.events

import com.simplito.kotlin.privmx_endpoint.model.CollectionItemChange

/**
 * Holds data of event that arrives when the collection is changed.
 *
 * @property moduleType         Type of the module
 * @property moduleId           ID of the module
 * @property affectedItemsCount Count of affected items
 * @property items              List of item changes
 */
data class CollectionChangedEventData(
    val moduleType: String,
    val moduleId: String,
    val affectedItemsCount: Long,
    val items: List<CollectionItemChange>
)