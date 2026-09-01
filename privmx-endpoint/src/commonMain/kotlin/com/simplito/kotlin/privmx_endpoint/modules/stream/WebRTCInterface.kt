package com.simplito.kotlin.privmx_endpoint.modules.stream

import com.simplito.kotlin.privmx_endpoint.model.stream.Key

/**
 * Interface of the WebRTC layer used by `StreamApiLow`.
 * An implementation has to be provided when joining a Stream Room and is responsible for managing the PeerConnections
 * of that Stream Room - one for publishing ("publisher") and one for receiving ("subscriber") Streams.
 */
interface WebRTCInterface {
    /**
     * Creates an SDP offer for the given PeerConnection and sets it as its local description.
     *
     * @param streamRoomId ID of the Stream Room the connection belongs to
     * @param connectionType type of the connection ("publisher" or "subscriber")
     *
     * @return created offer SDP
     */
    fun createOfferAndSetLocalDescription(
        streamRoomId: String,
        connectionType: String
    ): String

    /**
     * Sets the given session description as the remote description of the PeerConnection, creates an answer to it
     * and sets that answer as the PeerConnection's local description.
     *
     * @param streamRoomId ID of the Stream Room the connection belongs to
     * @param sdp received session description in the SDP format
     * @param type type of the received session description ("offer" or "answer")
     * @param connectionType type of the connection ("publisher" or "subscriber")
     *
     * @return created answer SDP
     */
    fun createAnswerAndSetDescriptions(
        streamRoomId: String,
        sdp: String,
        type: String,
        connectionType: String
    ): String

    /**
     * Sets the received answer as the remote description of the PeerConnection.
     *
     * @param streamRoomId ID of the Stream Room the connection belongs to
     * @param sdp received session description in the SDP format
     * @param type type of the received session description ("offer" or "answer")
     * @param connectionType type of the connection ("publisher" or "subscriber")
     */
    fun setAnswerAndSetRemoteDescription(
        streamRoomId: String,
        sdp: String,
        type: String,
        connectionType: String
    )

    /**
     * Assigns the media server session to the PeerConnection.
     * The session ID is needed to send the PeerConnection's ICE candidates (using trickle) to the server.
     *
     * @param streamRoomId ID of the Stream Room the connection belongs to
     * @param sessionId ID of the media server session assigned to the connection
     * @param connectionType type of the connection ("publisher" or "subscriber")
     */
    fun updateSessionId(streamRoomId: String, sessionId: Long?, connectionType: String)

    /**
     * Closes a single PeerConnection of the given Stream Room.
     *
     * @param streamRoomId ID of the Stream Room the connection belongs to
     * @param connectionType type of the connection to close ("publisher" or "subscriber")
     */
    fun close(streamRoomId: String, connectionType: String)

    /**
     * Closes all PeerConnections of the given Stream Room.
     *
     * @param streamRoomId ID of the Stream Room to close the connections of
     */
    fun closeAll(streamRoomId: String)

    /**
     * Replaces the keys used to encrypt and decrypt the data sent over the publisher and subscriber Streams.
     *
     * @param streamRoomId ID of the Stream Room to update the keys of
     * @param keys new set of encryption keys
     */
    fun updateKeys(streamRoomId: String, keys: List<Key>)
}