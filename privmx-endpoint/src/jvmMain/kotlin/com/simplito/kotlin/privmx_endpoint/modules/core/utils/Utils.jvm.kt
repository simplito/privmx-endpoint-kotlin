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

actual object Utils {
    init {
        LibLoader.load()
    }

    /**
     * Removes all trailing whitespace.
     *
     * @param data
     * @return copy of string with removed trailing whitespace
     */
    actual external fun trim(data: String): String

    /**
     * Splits a string using the provided delimiter.
     *
     * @param data      the string to be split
     * @param delimiter string which will be split
     * @return split parts
     */
    actual external fun split(data: String, delimiter: String): List<String>

    /**
     * Removes all whitespace from the left of given string.
     *
     * @param data reference to string
     * @return copy of string without whitespace at the beginning
     */
    actual external fun ltrim(data: String): String

    /**
     * Removes all whitespace from the right of given string.
     *
     * @param data string to check
     * @return copy of string without whitespace at the end
     */
    actual external fun rtrim(data: String): String
}