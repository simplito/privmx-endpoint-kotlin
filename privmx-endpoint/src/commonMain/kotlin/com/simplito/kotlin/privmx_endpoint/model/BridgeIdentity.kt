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
 * Bridge server identification details.
 *
 * @property url Bridge URL.
 * @property pubKey Bridge public Key.
 * @property instanceId Bridge instance Id given by PKI.
 */
data class BridgeIdentity (
    val url: String,
    val pubKey: String? = null,
    val instanceId: String? = null
)