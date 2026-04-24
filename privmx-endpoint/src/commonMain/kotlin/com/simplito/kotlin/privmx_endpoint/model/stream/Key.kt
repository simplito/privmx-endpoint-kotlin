package com.simplito.kotlin.privmx_endpoint.model.stream

data class Key(
    var keyId: String,
    var key: ByteArray,
    val type: KeyType
)