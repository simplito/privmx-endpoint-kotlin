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
package com.simplito.kotlin.privmx_endpoint.model.exceptions

import kotlin.jvm.JvmOverloads

/**
 * Thrown when a PrivMX Endpoint method encounters an exception.
 *
 * @param message     brief  information about exception
 * @property description detailed information about exception
 * @property scope       scope of this exception
 * @property code        unique code of this exception
 * @property name        special name for this exception
 */
class PrivmxException
@JvmOverloads
internal constructor(
    message: String,
    val description: String,
    val scope: String,
    private val code: Int,
    val name: String = ""
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
        get() = "{\n" +
                "\"name\" : \"" + name + "\",\n" +
                "\"scope\" : \"" + scope + "\",\n" +
                "\"msg\" : \"" + message + "\",\n" +
                "\"code\" : " + getCode() + ",\n" +
                "\"description\" : \"" + description + "\"\n" +
                "}\n"
}
