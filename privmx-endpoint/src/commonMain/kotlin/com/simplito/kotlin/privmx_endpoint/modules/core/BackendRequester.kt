package com.simplito.kotlin.privmx_endpoint.modules.core

/**
 * BackendRequester provides functions to call PrivMX Bridge API.
 * (See PrivMX Bridge API for more details)
 */
expect object BackendRequester {
    /**
     * Sends a request to PrivMX Bridge API using access token for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param accessToken token for authorization
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     */
    fun backendRequest(
        serverUrl: String,
        accessToken: String,
        method: String,
        paramsAsJson: String
    ): String

    /**
    * Sends request to PrivMX Bridge API.
    *
    * @param serverUrl PrivMX Bridge server URL
    * @param method API method to call
    * @param paramsAsJson API method's parameters in JSON format
    *
    * @return JSON String representing raw server response
    */
    fun backendRequest(
        serverUrl: String,
        method: String,
        paramsAsJson: String
    ): String

    /**
     * Sends a request to PrivMX Bridge API using pair of API KEY ID and API KEY SECRET for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param apiKeyId API KEY ID
     * @param apiKeySecret API KEY SECRET
     * @param mode allows you to set whether the request should be signed (mode = 1) or plain (mode = 0)
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     */
    fun backendRequest(
        serverUrl: String,
        apiKeyId: String,
        apiKeySecret: String,
        mode: Long,
        method: String,
        paramsAsJson: String
    ): String
}