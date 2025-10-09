package com.simplito.kotlin.privmx_endpoint.model.events

import com.simplito.kotlin.privmx_endpoint.model.File
import com.simplito.kotlin.privmx_endpoint.model.FileChange

/**
 * Holds information about file updates.
 *
 * @property file    File meta.
 * @property changes List of file changes.
 */
data class StoreFileUpdatedEventData(
    val file: File,
    val changes: List<FileChange>
)