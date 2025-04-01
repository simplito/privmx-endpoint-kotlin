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

/**
 * Thrown when a PrivMX Endpoint method encounters an exception.
 *
 * @category errors
 */
class PrivmxException
/**
 * Creates instance of `PrivmxException`.
 *
 * @param message     brief  information about exception
 * @param description detailed information about exception
 * @param scope       scope of this exception
 * @param code        unique code of this exception
 * @param name        special name for this exception
 */  internal constructor(
    override val message: String?,
    /**
     * Detailed description of the exception.
     */
    val description: String?,
    /**
     * Scope of the exception.
     */
    val scope: String?,
    /**
     * Code of the exception.
     */
    private val code: Int?,
    /**
     * Native Exception name.
     */
    val name: String?
) : RuntimeException(message) {
    /**
     * Creates instance of `PrivmxException`.
     *
     * @param message short information about exception
     * @param scope   scope of this exception
     * @param code    unique code of this exception
     */
    internal constructor(message: String?, scope: String?, code: Int) : this(
        message,
        null,
        scope,
        code,
        ""
    )

    /**
     * Creates instance of `PrivmxException`.
     *
     * @param message     short information about exception
     * @param description information about exception
     * @param scope       scope of this exception
     * @param code        unique code of this exception
     */
    internal constructor(message: String?, description: String?, scope: String?, code: Int) : this(
        message,
        description,
        scope,
        code,
        ""
    )

    /**
     * Returns full information about the exception.
     *
     *
     * See: [.getFull].
     *
     * @return Full information about exception
     */

    override fun toString(): String = this.full ?: ""

    /**
     * Returns exception code as `unsigned int` converted to `long`.
     *
     * @return Exception code
     */
    fun getCode(): Long {
        return code?.toULong()?.toLong() ?: 0
    }

    val full: String?
        /**
         * Returns full information about exception.
         *
         * @return Full information about exception
         */
        get() = "{\n" +
                "\"name\" : \"" + name + "\",\n" +
                "\"scope\" : \"" + scope + "\",\n" +
                "\"msg\" : \"" + message + "\",\n" +
                "\"code\" : " + getCode() + ",\n" +
                "\"description\" : \"" + description + "\"\n" +
                "}\n"
}
