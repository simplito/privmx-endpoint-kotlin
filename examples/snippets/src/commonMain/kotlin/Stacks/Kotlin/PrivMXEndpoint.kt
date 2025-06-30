package Stacks.Kotlin

import Stacks.Kotlin.events.eventApi
import Stacks.Kotlin.inboxes.inboxApi
import Stacks.Kotlin.stores.storeApi
import Stacks.Kotlin.threads.threadApi
import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpoint
import com.simplito.kotlin.privmx_endpoint_extra.lib.PrivmxEndpointContainer
import com.simplito.kotlin.privmx_endpoint_extra.model.Modules

lateinit var endpointContainer: PrivmxEndpointContainer
lateinit var endpointSession: PrivmxEndpoint

// START: Initial assumption snippet
/*
    All the values below like BRIDGE_URL, SOLUTION_ID, CONTEXT_ID
    should be replaced by the ones corresponding to your Bridge Server instance.

    The private keys here are for demonstration purposes only.
    Normally, they should be kept separately by each user and stored in a safe place,
    or generated from a password (see the derivePrivateKey2() method in the Crypto API)
*/

val bridgeUrl = "YOUR_BRIDGE_URL"
val solutionId = "YOUR_SOLUTION_ID"
val contextId = "YOUR_CONTEXT_ID"

val user1Id = "USER_ID_1"
val user1PublicKey = "PUBLIC_KEY_1"
val user1PrivateKey = "PRIVATE_KEY_1"

val user2Id = "USER_ID_2"
val user2PublicKey = "PUBLIC_KEY_2"
// END: Initial assumption snippet


fun makeConnection(){
    // START: Make connection snippet
    val pathToCerts = "PATH_TO_CERTS" // Path to .pem ssl certificate to connect with Privmx Bridge
    val initModules = setOf(
        Modules.THREAD, // initializes ThreadApi to working with Threads
        Modules.STORE, // initializes StoreApi to working with Stores
        Modules.INBOX, // initializes InboxApi to working with Inboxes
        Modules.CUSTOM_EVENT // initializes EventApi to working with Custom Events
    ) // set of modules to activate in new connection

    val endpointContainer = PrivmxEndpointContainer().also {
        it.setCertsPath(pathToCerts)
    }

    val endpointSession = endpointContainer.connect(
        initModules,
        user1PrivateKey,
        solutionId,
        bridgeUrl
    )
    // END: Make connection snippet

    setupConnection(endpointContainer,endpointSession)
}

fun setUserVerifier() {
    val userVerifier = UserVerifierInterface { requests ->
        requests.map { request ->
            // Your verification code for the request
            true
        }
    }

    endpointSession.connection.setUserVerifier(userVerifier)
}

fun getEndpoint(){
    endpointContainer.getEndpoint(endpointSession.connection.getConnectionId()!!)
}

fun close(){
    endpointContainer.close()
}

fun disconnectAll(){
    endpointContainer.disconnectAll()
}

fun disconnectById(){
    endpointContainer.disconnect(endpointSession.connection.getConnectionId()!!)
}

//setup global connection variables
private fun setupConnection(ct: PrivmxEndpointContainer, conn: PrivmxEndpoint){
    endpointContainer = ct
    endpointSession = conn
    threadApi = endpointSession.threadApi!!
    storeApi = endpointSession.storeApi!!
    inboxApi = endpointSession.inboxApi!!
    eventApi = endpointSession.eventApi!!
}