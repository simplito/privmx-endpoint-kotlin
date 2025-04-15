package com.simplito.kotlin.privmx_endpoint.modules.core

expect object BackendRequester {
    fun backendRequest(
        serverUrl: String,
        accessToken: String,
        method: String,
        paramsAsJson: String
    ): String

    fun backendRequest(
        serverUrl: String,
        method: String,
        paramsAsJson: String
    ): String

    fun backendRequest(
        serverUrl: String,
        apiKeyId: String,
        apiKeySecret: String,
        mode: Long,
        method: String,
        paramsAsJson: String
    ): String
}