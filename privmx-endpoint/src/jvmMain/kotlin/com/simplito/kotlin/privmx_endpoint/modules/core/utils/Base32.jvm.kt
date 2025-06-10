package com.simplito.kotlin.privmx_endpoint.modules.core.utils

import com.simplito.kotlin.privmx_endpoint.LibLoader

actual object Base32 {
    init {
        LibLoader.load()
    }

    /**
     * Encodes buffer to string in Base32 format.
     *
     * @param data buffer to encode
     * @return string in Base32 format
     */
    actual external fun encode(data: ByteArray): String

    /**
     * Decodes string in Base32 to buffer.
     *
     * @param base32Data string to decode
     * @return buffer with decoded data
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