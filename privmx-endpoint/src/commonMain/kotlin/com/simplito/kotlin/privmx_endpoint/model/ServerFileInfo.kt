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
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Holds file's information created by server.
 *
 * @category store
 * @group Store
 */
data class ServerFileInfo
/**
 * Creates instance of `ServerFileInfo`.
 *
 * @param storeId    ID of the Store.
 * @param fileId     ID of the file.
 * @param createDate File's creation timestamp.
 * @param author     ID of the user who created the file.
 */(
    /**
     * ID of the Store.
     */
    val storeId: String,
    /**
     * ID of the file.
     */
    val fileId: String,
    /**
     * File's creation timestamp.
     */
    val createDate: Long?,
    /**
     * ID of the user who created the file.
     */
    val author: String
)
