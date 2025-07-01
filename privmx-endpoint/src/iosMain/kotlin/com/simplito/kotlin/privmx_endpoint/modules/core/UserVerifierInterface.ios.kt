//
// PrivMX Endpoint Kotlin.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.simplito.kotlin.privmx_endpoint.modules.core

import cnames.structs.pson_value
import com.simplito.kotlin.privmx_endpoint.modules.core.UserVerifierInterface
import com.simplito.kotlin.privmx_endpoint.utils.PsonValue
import com.simplito.kotlin.privmx_endpoint.utils.asArgs
import com.simplito.kotlin.privmx_endpoint.utils.pson
import com.simplito.kotlin.privmx_endpoint.utils.toVerificationRequest
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.value

@OptIn(ExperimentalForeignApi::class)
internal fun UserVerifierInterface.verifier(
    args: CPointer<pson_value>?,
    res: CPointer<CPointerVarOf<CPointer<pson_value>>>?
): Int = memScoped {
    try {
        @Suppress("UNCHECKED_CAST")
        val argsK = (args!!.asArgs as PsonValue.PsonArray<PsonValue.PsonObject>)
            .getValue()
            .map { it.toVerificationRequest() }
        val resK = verify(argsK)

        val resC = resK.map {
            it.pson
        }.pson.toNativePson()
        res?.pointed?.value = resC

        return@memScoped 0
    } catch (_: Exception) {
        res?.pointed?.value = (args!!.asArgs).getValue().map {
            false.pson
        }.pson.toNativePson()
        return@memScoped -1
    }
}