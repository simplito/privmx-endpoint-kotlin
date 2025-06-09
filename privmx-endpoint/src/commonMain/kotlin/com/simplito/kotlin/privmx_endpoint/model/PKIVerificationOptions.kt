package com.simplito.kotlin.privmx_endpoint.model

/**
 * PrivMX Bridge server instance verification options using a PKI server.
 *
 * @property bridgePubKey Bridge public Key.
 * @property bridgeInstanceId Bridge instance Id given by PKI.
 */
class PKIVerificationOptions (
    /**
     * Bridge public Key.
     */
    var bridgePubKey: String? = null,

    /**
     * Bridge instance Id given by PKI.
     */
    var bridgeInstanceId: String? = null
)