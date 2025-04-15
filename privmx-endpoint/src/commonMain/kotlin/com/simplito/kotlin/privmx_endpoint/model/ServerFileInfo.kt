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
 * @property storeId    ID of the Store.
 * @property fileId     ID of the file.
 * @property createDate File's creation timestamp.
 * @property author     ID of the user who created the file.
 *
 * @category store
 * @group Store
 */
class ServerFileInfo(
    var storeId: String?,
    var fileId: String?,
    var createDate: Long?,
    var author: String?
)
