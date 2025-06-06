package com.simplito.kotlin.privmx_endpoint.model

/**
 * Options used to verify if Bridge on given url is the one you expect.
 *
 * @property bridgePubKey Bridge public Key
 * @property bridgeInstanceId Bridge instance Id given by PKI
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