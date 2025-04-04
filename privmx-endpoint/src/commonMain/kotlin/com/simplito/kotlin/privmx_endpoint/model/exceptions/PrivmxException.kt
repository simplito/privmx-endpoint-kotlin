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
 * @param description detailed information about exception
 * @param scope       scope of this exception
 * @param code        unique code of this exception
 * @param name        special name for this exception
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
     * Returns exception code as `unsigned int` converted to `long`.
     *
     * @return Exception code
     */
    fun getCode(): Long = code.toULong().toLong()

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
