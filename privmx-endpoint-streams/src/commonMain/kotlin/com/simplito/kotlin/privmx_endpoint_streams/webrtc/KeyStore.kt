package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import com.simplito.kotlin.privmx_endpoint.model.stream.Key

expect class KeyStore

internal expect fun createKeyStore(): KeyStore
internal expect fun KeyStore.applyKeys(keys: List<Key>)