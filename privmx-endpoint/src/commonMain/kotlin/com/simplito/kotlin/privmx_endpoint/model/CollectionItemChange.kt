package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains information about the changed item in the collection.
 *
 * @property itemId ID of the item
 * @property action Item change action, which can be "create", "update" or "delete"
 */
class CollectionItemChange(
    val itemId: String,
    val action: String
)