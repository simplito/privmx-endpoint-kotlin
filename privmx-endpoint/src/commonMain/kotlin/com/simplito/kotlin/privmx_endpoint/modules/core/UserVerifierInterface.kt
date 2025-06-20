package com.simplito.kotlin.privmx_endpoint.modules.core

import com.simplito.kotlin.privmx_endpoint.model.VerificationRequest

/**
 * UserVerifierInterface - an interface consisting of a single verify() method, which - when implemented - should perform verification of the provided data using an external service verification
 * should be done using an external service such as an application server or a PKI server.
 */
fun interface UserVerifierInterface {
    /**
     * Verifies whether the specified users are valid.
     * Checks if each user belonged to the Context and if this is their key in `date` and return `true` or `false` otherwise.
     *
     * @param request List of user data to verification
     * @return List of verification results whose items correspond to the items in the input list
     */
    fun verify(request: List<VerificationRequest>): List<Boolean>
}