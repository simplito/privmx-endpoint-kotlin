package com.simplito.kotlin.privmx_endpoint.modules.core.utils

expect object Hex {
    /**
     * Encodes buffer to a string in Hex format.
     *
     * @param data buffer to encode
     * @return string in Hex format
     */
    fun encode(data: ByteArray): String

    /**
     * Decodes string in Hex to buffer.
     *
     * @param hexData string to decode
     * @return buffer with decoded data
     */
    fun decode(hexData: String): ByteArray

    /**
     * Checks if given string is in Hex format.
     *
     * @param data string to check
     * @return data check result
     */
    fun `is`(data: String): Boolean
}
