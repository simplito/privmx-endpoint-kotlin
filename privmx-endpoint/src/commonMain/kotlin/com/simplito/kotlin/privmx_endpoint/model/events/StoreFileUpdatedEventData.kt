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