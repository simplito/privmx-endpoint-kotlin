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
 * @param storeId    ID of the Store.
 * @param fileId     ID of the file.
 * @param createDate File's creation timestamp.
 * @param author     ID of the user who created the file.
 *
 * @category store
 * @group Store
 */
class ServerFileInfo(
    /**
     * ID of the Store.
     */
    var storeId: String?,
    /**
     * ID of the file.
     */
    var fileId: String?,
    /**
     * File's creation timestamp.
     */
    var createDate: Long?,
    /**
     * ID of the user who created the file.
     */
    var author: String?
)
