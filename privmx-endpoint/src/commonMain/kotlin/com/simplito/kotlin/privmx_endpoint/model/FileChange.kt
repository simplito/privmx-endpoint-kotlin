/*
 *
 * PrivMX Endpoint Kotlin.
 * Copyright © 2025 Simplito sp. z o.o.
 *
 * This file is part of the PrivMX Platform (https://privmx.dev).
 * This software is Licensed under the MIT License.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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