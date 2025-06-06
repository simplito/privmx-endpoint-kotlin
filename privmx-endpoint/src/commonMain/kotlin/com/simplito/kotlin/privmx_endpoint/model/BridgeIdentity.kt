package com.simplito.kotlin.privmx_endpoint.model

/**
 * Data send by client to all data informing where was it send
 *
 * @property url Bridge URL
 * @property pubKey Bridge public Key
 * @property instanceId Bridge instance Id given by PKI
 */
class BridgeIdentity (
    val url: String,
    var pubKey: String? = null,
    var instanceId: String? = null
)