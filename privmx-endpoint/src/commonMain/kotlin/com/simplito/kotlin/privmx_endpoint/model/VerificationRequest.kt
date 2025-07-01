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
 *
 * @property contextId    ID of the Context.
 * @property senderId     ID of the sender.
 * @property senderPubKey Public key of the sender.
 * @property date         Creation date of the data.
 * @property bridgeIdentity Bridge Identity.
*/
data class VerificationRequest(
    val contextId: String,
    val senderId: String,
    val senderPubKey: String,
    val date: Long?,
    val bridgeIdentity: BridgeIdentity? = null
)