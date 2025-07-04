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
 * PrivMX Bridge server instance verification options using a PKI server.
 *
 * @property bridgePubKey Bridge public Key.
 * @property bridgeInstanceId Bridge instance Id given by PKI.
 */
data class PKIVerificationOptions (
    val bridgePubKey: String? = null,
    val bridgeInstanceId: String? = null
)