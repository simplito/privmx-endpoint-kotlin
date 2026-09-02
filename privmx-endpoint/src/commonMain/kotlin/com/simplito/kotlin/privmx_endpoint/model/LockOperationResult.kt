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

package com.simplito.kotlin.privmx_endpoint.model

/**
 * Result of a lock or unlock operation.
 *
 * @property success Whether the requested lock level was successfully acquired/released
 * @property currentLevel The lock level currently held by the caller after the operation
 */
data class LockOperationResult(
    val success: Boolean,
    val currentLevel: LockLevel?
)
