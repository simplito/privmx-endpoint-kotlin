package com.simplito.java.privmx_endpoint.modules.core

actual class BackendRequester {
    actual companion object {
        init {
            System.loadLibrary("privmx-endpoint-java")
        }
        @JvmStatic
        actual external fun backendRequest(
            serverUrl: String,
            accessToken: String,
            method: String,
            paramsAsJson: String
        ): String?

        @JvmStatic
        actual external fun backendRequest(
            serverUrl: String,
            method: String,
            paramsAsJson: String
        ): String?

        @JvmStatic
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