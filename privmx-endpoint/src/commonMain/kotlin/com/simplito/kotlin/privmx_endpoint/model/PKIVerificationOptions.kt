package com.simplito.kotlin.privmx_endpoint.model

/**
 * PrivMX Bridge server instance verification options using a PKI server.
 *
 * @property bridgePubKey Bridge public Key.
 * @property bridgeInstanceId Bridge instance Id given by PKI.
 */
data class PKIVerificationOptions (
    val bridgePubKey: String? = null,
    val bridgeInstanceId: String? = null
)