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
 * Lock level describing the type of lock held on a resource.
 */
enum class LockLevel {
    /**
     * No lock held.
     */
    NONE,

    /**
     * Shared (reader) lock — multiple holders allowed.
     */
    SHARED,

    /**
     * Reserved lock — signals intent to escalate to exclusive.
     */
    RESERVED,

    /**
     * Pending lock — waiting for readers to finish before escalating.
     */
    PENDING,

    /**
     * Exclusive (writer) lock — single holder, no other locks allowed.
     */
    EXCLUSIVE
}
