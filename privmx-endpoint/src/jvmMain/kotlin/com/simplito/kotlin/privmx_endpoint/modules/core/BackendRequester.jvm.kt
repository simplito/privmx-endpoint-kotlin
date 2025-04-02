package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.LibLoader

actual class BackendRequester {
    actual companion object {
        init {
            LibLoader.load()
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