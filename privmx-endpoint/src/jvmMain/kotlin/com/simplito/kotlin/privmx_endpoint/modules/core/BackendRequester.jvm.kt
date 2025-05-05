package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.LibLoader
import com.simplito.kotlin.privmx_endpoint.model.exceptions.NativeException
import com.simplito.kotlin.privmx_endpoint.model.exceptions.PrivmxException
import kotlin.Throws

/**
 * 'BackendRequester' provides functions to call PrivMX Bridge API.
 */
actual object BackendRequester {
    init {
        LibLoader.load()
    }

    /**
     * Sends a request to PrivMX Bridge API using access token for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param accessToken token for authorization (see PrivMX Bridge API for more details)
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     * @throws PrivmxException thrown when method encounters an exception.
     * @throws NativeException thrown when method encounters an unknown exception.
     */
    @Throws(PrivmxException::class, NativeException::class)
    @JvmStatic
    actual external fun backendRequest(
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
    @JvmStatic
    actual external fun backendRequest(
        serverUrl: String,
        method: String,
        paramsAsJson: String
    ): String

    /**
     * Sends a request to PrivMX Bridge API using pair of API KEY ID and API KEY SECRET for authorization.
     *
     * @param serverUrl PrivMX Bridge server URL
     * @param apiKeyId API KEY ID (see PrivMX Bridge API for more details)
     * @param apiKeySecret API KEY SECRET (see PrivMX Bridge API for more details)
     * @param mode allows you to set whether the request should be signed (mode = 1) or plain (mode = 0)
     * @param method API method to call
     * @param paramsAsJson API method's parameters in JSON format
     *
     * @return JSON String representing raw server response
     */
    @JvmStatic
    actual external fun backendRequest(
        serverUrl: String,
        apiKeyId: String,
        apiKeySecret: String,
        mode: Long,
        method: String,
        paramsAsJson: String
    ): String

}