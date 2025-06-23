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

import com.simplito.kotlin.privmx_endpoint.LibLoader

actual object Base32 {
    init {
        LibLoader.load()
    }

    /**
     * Encodes byte array to string in Base32 format.
     *
     * @param data byte array to encode
     * @return string in Base32 format
     */
    actual external fun encode(data: ByteArray): String

    /**
     * Decodes string in Base32 to byte array.
     *
     * @param base32Data string to decode
     * @return byte array with decoded data
     */
    actual external fun decode(base32Data: String): ByteArray

    /**
     * Checks if given string is in Base32 format.
     *
     * @param data string to check
     * @return data check result
     */
    actual external fun `is`(data: String): Boolean
}