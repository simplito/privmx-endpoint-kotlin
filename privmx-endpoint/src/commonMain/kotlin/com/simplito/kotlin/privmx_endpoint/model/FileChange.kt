package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds information about the file change.
 *
 * @property pos      Position of the first changed chunk.
 * @property length   Length aligned to full chunks.
 * @property  truncate Remove all data.
 */
class FileChange(
    var pos: Long?,
    var length: Long?,
    var truncate: Boolean
)