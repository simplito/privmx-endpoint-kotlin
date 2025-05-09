package com.simplito.java.privmx_endpoint.model.utils

expect object Base64 {
    /**
     * Encodes buffer to string in Base64 format.
     *
     * @param data buffer to encode
     * @return string in Base64 format
     */
    fun encode(data: ByteArray): String

    /**
     * Decodes string in Base64 to buffer.
     *
     * @param base64Data string to decode
     * @return buffer with decoded data
     */
    fun decode(base64Data: String): ByteArray

    /**
     * Checks if given string is in Base64 format.
     *
     * @param data string to check
     * @return data check result
     */
    fun `is`(data: String): Boolean
}
