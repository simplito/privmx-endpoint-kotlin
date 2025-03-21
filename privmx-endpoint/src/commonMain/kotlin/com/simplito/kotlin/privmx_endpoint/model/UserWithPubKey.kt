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
 * Contains ID of user and the corresponding public key.
 *
 * @category core
 * @group Core
 */
class UserWithPubKey {
    /**
     * ID of the user.
     */
    var userId: String? = null

    /**
     * User's public key.
     */
    var pubKey: String? = null

    /**
     * Creates instance of `UserWithPubKey`.
     */
    constructor()

    /**
     * Creates instance of `UserWithPubKey`.
     * @param userId ID of the user.
     * @param pubKey User's public key.
     */
    constructor(
        userId: String?,
        pubKey: String?
    ) {
        this.userId = userId
        this.pubKey = pubKey
    }
}
