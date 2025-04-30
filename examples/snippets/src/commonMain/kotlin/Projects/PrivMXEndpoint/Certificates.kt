package Projects.PrivMXEndpoint

import com.simplito.java.privmx_endpoint_extra.lib.PrivmxEndpointContainer

fun setCertificate(){
    val endpointContainer = PrivmxEndpointContainer()
    endpointContainer.setCertsPath("YOUR-PATH-TO-CERTS-FILE.pem")
}