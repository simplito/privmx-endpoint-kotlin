package webrtc

expect class FrameCryptor
expect class PmxFrameCryptorOptions(dropFrameIfCryptionFailed: Boolean)

internal expect fun FrameCryptor.disposeCryptor()
internal expect fun FrameCryptor.applyOptions(options: PmxFrameCryptorOptions)