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
 * Which key an envelope was sealed with, and therefore what its author field is worth.
 */
enum class EnvelopeType {
    /**
     * Sealed with the Group's symmetric data key by a member, and signed by them.
     */
    ENVELOPE_FROM_MEMBER,

    /**
     * Sealed to the Group's identity public key by an outsider using a throwaway keypair. Unattributable.
     */
    ENVELOPE_ANONYMOUS
}
