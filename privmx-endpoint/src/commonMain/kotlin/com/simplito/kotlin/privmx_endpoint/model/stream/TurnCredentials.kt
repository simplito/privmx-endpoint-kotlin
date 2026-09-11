package com.simplito.kotlin.privmx_endpoint.model.stream

 data class TurnCredentials(
     val url: String,
     val username: String,
     val password: String,
     val expirationTime: Long?
 )