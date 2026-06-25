@file:OptIn(ExperimentalForeignApi::class)

package webrtc

import WebRTCFramework.PMXKSKey
import WebRTCFramework.PMXKSKeyTypeLOCAL
import WebRTCFramework.PMXKSKeyTypeREMOTE
import WebRTCFramework.PMXKeyStore
import com.simplito.kotlin.privmx_endpoint.model.stream.Key
import com.simplito.kotlin.privmx_endpoint.model.stream.KeyType
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes

actual typealias KeyStore = PMXKeyStore

internal actual fun createKeyStore(): KeyStore = PMXKeyStore()

internal actual fun KeyStore.applyKeys(keys: List<Key>) {
    setKeys(keys.map { key ->
        PMXKSKey(
            key.keyId,
            key.key.usePinned { NSData.dataWithBytes(it.addressOf(0), it.get().size.toULong()) },
            if (key.type == KeyType.LOCAL) PMXKSKeyTypeLOCAL else PMXKSKeyTypeREMOTE
        )
    })
}