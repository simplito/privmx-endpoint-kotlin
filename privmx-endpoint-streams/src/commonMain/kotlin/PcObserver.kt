expect class PcObserver(
    peerConnectionFactory: PeerConnectionFactory,
    keyStore: KeyStore,
    trackObserver: TrackObserver?,
    onIceCandidateCallback: (candidate: IceCandidate) -> Unit,
    onRenegotiationNeededCallback: () -> Unit = {},
    onIceConnectionChangeCallback: (candidate: IceConnectionState) -> Unit = {}
): Observer{
    fun setFrameCryptorOptions(options: PmxFrameCryptorOptions)
    fun dispose()
}
