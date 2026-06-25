package webrtc

import org.webrtc.PmxFrameCryptor

actual typealias FrameCryptor = PmxFrameCryptor

actual class PmxFrameCryptorOptions actual constructor(
    dropFrameIfCryptionFailed: Boolean
) : PmxFrameCryptor.PmxFrameCryptorOptions() {
    init {
        this.dropFrameIfCryptionFailed = dropFrameIfCryptionFailed
    }
}

internal actual fun FrameCryptor.disposeCryptor() = dispose()
internal actual fun FrameCryptor.applyOptions(options: PmxFrameCryptorOptions) = setOptions(options)