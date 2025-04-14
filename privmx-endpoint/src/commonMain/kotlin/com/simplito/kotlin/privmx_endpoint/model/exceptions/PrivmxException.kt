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
package com.simplito.kotlin.privmx_endpoint.model.exceptions

/**
 * Thrown when a PrivMX Endpoint method encounters an exception.
 *
 * @param message     brief  information about exception
 * @property description detailed information about exception
 * @property scope       scope of this exception
 * @property code        unique code of this exception
 * @property name        special name for this exception
 *
 * @category errors
 */
class PrivmxException internal constructor(
    message: String?,
    val description: String? = null,
    val scope: String? = null,
    private val code: Int,
    val name: String? = ""
) : RuntimeException(message) {
    /**
     * Returns full information about the exception.
     *
     * @return Full information about the exception as a JSON-like string
     */
    override fun toString(): String = full

    /**
     * Returns exception code as [UInt].
     *
     * @return Exception code
     */
    fun getCode(): UInt = code.toUInt()

    /**
     * Returns full information about exception as a formatted string.
     *
     * @return Full information about exception in JSON-like format
     */
    val full: String
        get() = buildString {
            append("{\n")
            append("\"name\" : \"$name\",\n")
            append("\"scope\" : \"$scope\",\n")
            append("\"msg\" : \"$message\",\n")
            append("\"code\" : ${getCode()},\n")
            append("\"description\" : \"$description\"\n")
            append("}\n")
        }
}
