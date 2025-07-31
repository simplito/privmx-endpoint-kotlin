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
 * Holds KVDB entry's information created by the server.
 *
 * @param kvdbId ID of the KVDB
 * @param key KVDB entry's key
 * @param createDate Entry's creation timestamp
 * @param author ID of the user who created the entry
 */
class ServerKvdbEntryInfo(
    val kvdbId: String,
    val key: String,
    val createDate: Long?,
    val author: String
) 