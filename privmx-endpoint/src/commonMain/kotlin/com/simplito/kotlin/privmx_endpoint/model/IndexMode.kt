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
 * Defines the mode in which the Search Index operates, specifically regarding
 * the storage and retrieval of document content.
 */
enum class IndexMode {
    /**
     * IndexMode is UNKNOWN or data is unreadable (check statusCode).
     */
    UNKNOWN,

    /**
     * The Search Index stores the full document content internally.
     * When searching, the full content field of the [Document] will be returned.
     * This mode requires more storage but simplifies content retrieval.
     */
    WITH_CONTENT,

    /**
     * The Search Index only stores metadata and terms necessary for search,
     * but discards the original document content.
     * When searching, the content field of the returned [Document] will be empty.
     * This mode saves storage space, assuming content is retrieved from an external source
     * (e.g. `StoreApi`) using the Document ID and name.
     */
    WITHOUT_CONTENT
}
