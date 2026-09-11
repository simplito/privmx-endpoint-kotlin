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
 * Represents a document for indexing.
 *
 * @property documentId Document ID
 * @property name Document name
 * @property content Document content
 */
data class Document(
    val documentId: Long,
    val name: String,
    val content: String
)
