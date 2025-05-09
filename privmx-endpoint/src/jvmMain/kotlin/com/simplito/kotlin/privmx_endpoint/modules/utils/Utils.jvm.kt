package com.simplito.java.privmx_endpoint.model.utils

actual object Utils {
    /**
     * Removes all trailing whitespace.
     *
     * @param data
     * @return copy of string with removed trailing whitespace.
     */
    actual external fun trim(data: String): String

    /**
     * Splits string by given delimiter (delimiter is removed).
     *
     * @param data      string to split
     * @param delimiter string which will be split
     * @return vector containing all split parts
     */
    actual external fun split(data: String, delimiter: String): List<String>

    /**
     * Removes all whitespace from the left of given string.
     *
     * @param data reference to string
     */
    actual external fun ltrim(data: String): String

    /**
     * Removes all whitespace from the right of given string.
     *
     * @param data string to check
     */
    actual external fun rtrim(data: String): String
}