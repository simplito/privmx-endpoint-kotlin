//
// PrivMX Endpoint Kotlin Extra.
// Copyright © 2025 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint_extra.model

/**
 * Available modules for PrivMX Endpoint.
 */
enum class Modules {
    /**
     * Thread module case.
     */
    THREAD,

    /**
     * Store module case.
     */
    STORE,

    /**
     * Inbox module case.
     */
    INBOX,

    /**
     * CustomEvent module case.
     */
    CUSTOM_EVENT,

    /**
     * Kvdb module case.
     */
    KVDB,
}
