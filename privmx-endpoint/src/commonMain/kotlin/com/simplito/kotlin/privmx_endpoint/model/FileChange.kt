package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds information about the file change.
 *
 * @property pos      Position of file change.
 * @property length   Length of file change.
 * @property  truncate Remove all data.
 */
class FileChange(
    var pos: Long?,
    var length: Long?,
    var truncate: Boolean
)