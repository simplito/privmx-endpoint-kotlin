//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.java.privmx_endpoint.model

/**
 * Holds Inbox files configuration.
 *
 * @category inbox
 * @group Inbox
 */
class FilesConfig
/**
 * Creates instance of `FilesConfig`.
 *
 * @param minCount           Minimum number of files required when sending Inbox entry.
 * @param maxCount           Maximum number of files allowed when sending Inbox entry.
 * @param maxFileSize        Maximum file size allowed when sending Inbox entry.
 * @param maxWholeUploadSize Maximum size of all files in total allowed when sending Inbox entry.
 */(
    /**
     * Minimum number of files required when sending Inbox entry.
     */
    var minCount: Long?,
    /**
     * Maximum number of files allowed when sending Inbox entry.
     */
    var maxCount: Long?,
    /**
     * Maximum file size allowed when sending Inbox entry.
     */
    var maxFileSize: Long?,
    /**
     * Maximum size of all files in total allowed when sending Inbox entry.
     */
    var maxWholeUploadSize: Long?
)
