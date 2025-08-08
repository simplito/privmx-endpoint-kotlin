package com.simplito.kotlin.privmx_endpoint.model.events

import com.simplito.kotlin.privmx_endpoint.model.FileChange
import com.simplito.kotlin.privmx_endpoint.model.File

/**
 * Holds information about the file change.
 *
 * @property file    File meta.
 * @property changes List of file changes.
 */
class StoreFileUpdatedEventData(
    file: File,
    changes: List<FileChange>
)