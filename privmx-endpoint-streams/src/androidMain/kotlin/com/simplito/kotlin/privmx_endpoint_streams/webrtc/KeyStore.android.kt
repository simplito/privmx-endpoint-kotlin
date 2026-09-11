package com.simplito.kotlin.privmx_endpoint_streams.webrtc

import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import org.webrtc.PmxFrameCryptorFactory
import org.webrtc.PmxKeyStore

actual typealias KeyStore = PmxKeyStore

internal actual fun createKeyStore(): KeyStore = PmxFrameCryptorFactory.createPmxKeyStore()

internal actual fun KeyStore.applyKeys(keys: List<Key>) {
    setKeys(keys.map { key ->
        PmxKeyStore.Key(
            key.keyId,
            key.key,
            if (key.type == KeyType.LOCAL) PmxKeyStore.KeyType.LOCAL else PmxKeyStore.KeyType.REMOTE
        )
    })
}