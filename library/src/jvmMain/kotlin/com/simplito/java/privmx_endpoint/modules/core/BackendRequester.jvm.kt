package com.simplito.java.privmx_endpoint.modules.core

actual class BackendRequester {
    actual companion object {
        actual external fun backendRequest(
            serverUrl: String,
            accessToken: String,
            method: String,
            paramsAsJson: String
        ): String?

        actual external fun backendRequest(
            serverUrl: String,
            method: String,
            paramsAsJson: String
        ): String?

        actual external fun backendRequest(
            serverUrl: String,
            apiKeyId: String,
            apiKeySecret: String,
            mode: Long,
            method: String,
            paramsAsJson: String
        ): String?
    }
}