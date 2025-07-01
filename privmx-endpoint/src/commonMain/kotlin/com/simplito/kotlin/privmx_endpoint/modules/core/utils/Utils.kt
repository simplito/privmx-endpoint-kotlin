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

expect object Utils {
    /**
     * Removes all trailing whitespace.
     *
     * @param data
     * @return copy of string with removed trailing whitespace.
     */
    fun trim(data: String): String

    /**
     * Splits string by given delimiter (delimiter is removed).
     *
     * @param data      string to split
     * @param delimiter string which will be split
     * @return list containing all split parts
     */
    fun split(data: String, delimiter: String): List<String>

    /**
     * Removes all whitespace from the left of given string.
     *
     * @param data reference to string
     */
    fun ltrim(data: String): String

    /**
     * Removes all whitespace from the right of given string.
     *
     * @param data string to check
     */
    fun rtrim(data: String): String
}