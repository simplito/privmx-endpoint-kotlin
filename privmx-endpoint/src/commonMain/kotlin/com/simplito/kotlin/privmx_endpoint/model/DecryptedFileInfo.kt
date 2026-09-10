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
 * What could be established about a sealed file, once all of it has been received.
 *
 * @property groupId ID of the Group the file was sealed for
 * @property authorPubKey Public key of the author (base58-DER encoded), whose signature over the file header has
 * been verified. EMPTY when [type] is [EnvelopeType.ENVELOPE_ANONYMOUS]
 * @property type Whether the file came from a member or from an anonymous outsider
 * @property complete Whether the whole file was verified to be present. False once
 * [com.simplito.kotlin.privmx_endpoint.modules.group.GroupApi.seekInEncryptedFile] has been used
 */
data class DecryptedFileInfo(
    val groupId: String,
    val authorPubKey: String,
    val type: EnvelopeType,
    val complete: Boolean
)
