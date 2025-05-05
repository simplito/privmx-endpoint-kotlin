//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds Inbox files configuration.
 *
 * @property minCount           Minimum number of files required when sending Inbox entry
 * @property maxCount           Maximum number of files allowed when sending Inbox entry
 * @property maxFileSize        Maximum file size allowed when sending Inbox entry
 * @property maxWholeUploadSize Maximum size of all files in total allowed when sending Inbox entry
 */
data class FilesConfig(
    val minCount: Long?,
    val maxCount: Long?,
    val maxFileSize: Long?,
    val maxWholeUploadSize: Long?
)