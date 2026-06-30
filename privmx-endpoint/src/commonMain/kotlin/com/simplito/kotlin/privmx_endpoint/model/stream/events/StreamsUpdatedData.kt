package com.simplito.kotlin.privmx_endpoint.model.stream.events

/**
 * Data received when a new user joins the StreamRoom.
 * Contains a snapshot of all currently active streams in the room.
 *
 * @property room Identifier of the StreamRoom
 * @property streams List of streams in the room
 */
data class StreamsUpdatedData(
    val room: String,
    val streams: List<UpdatedStreamData>,
)