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
package com.simplito.kotlin.privmx_endpoint.model.exceptions

import kotlin.jvm.JvmOverloads

/**
 * Thrown when a PrivMX Endpoint method encounters an unknown exception.
 *
 * @param message information about the exception
 *
 * @category errors
 */
class NativeException
@JvmOverloads
internal constructor(message: String = "No message") : RuntimeException(message)
