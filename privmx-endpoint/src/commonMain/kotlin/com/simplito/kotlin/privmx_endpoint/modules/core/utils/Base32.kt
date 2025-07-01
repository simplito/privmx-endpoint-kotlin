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

package com.simplito.kotlin.privmx_endpoint.modules.core.utils

expect object Base32 {
    /**
     * Encodes buffer to string in Base32 format.
     *
     * @param data buffer to encode
     * @return string in Base32 format
     */
    fun encode(data: ByteArray): String

    /**
     * Decodes string in Base32 to buffer.
     *
     * @param base32Data string to decode
     * @return buffer with decoded data
     */
    fun decode(base32Data: String): ByteArray

    /**
     * Checks if given string is in Base32 format.
     *
     * @param data string to check
     * @return data check result
     */
    fun `is`(data: String): Boolean
}