package com.simplito.java.privmx_endpoint.model.utils

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
     * @return vector containing all split parts
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