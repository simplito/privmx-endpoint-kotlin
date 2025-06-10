package com.simplito.kotlin.privmx_endpoint.modules.core.utils

actual object Base64 {
    /**
     * Encodes buffer to string in Base64 format.
     *
     * @param data buffer to encode
     * @return string in Base64 format
     */
    actual external fun encode(data: ByteArray): String

    /**
     * Decodes string in Base64 to buffer.
     *
     * @param base64Data string to decode
     * @return buffer with decoded data
     */
    actual external fun decode(base64Data: String): ByteArray

    /**
     * Checks if given string is in Base64 format.
     *
     * @param data string to check
     * @return data check result
     */
    actual external fun `is`(data: String): Boolean
}
