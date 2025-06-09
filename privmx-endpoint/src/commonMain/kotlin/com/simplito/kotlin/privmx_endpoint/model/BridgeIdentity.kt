package com.simplito.kotlin.privmx_endpoint.model

/**
 * Bridge server identification details.
 *
 * @property url Bridge URL.
 * @property pubKey Bridge public Key.
 * @property instanceId Bridge instance Id given by PKI.
 */
class BridgeIdentity (
    val url: String,
    var pubKey: String? = null,
    var instanceId: String? = null
)