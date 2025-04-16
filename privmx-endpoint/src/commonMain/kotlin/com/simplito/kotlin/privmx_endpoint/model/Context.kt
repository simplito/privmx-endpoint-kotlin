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
 * Contains base Context information.
 * @category core
 * @group Core
 */
data class Context(
    /**
     * ID of the user requesting information.
     */
    val userId: String,
    /**
     * ID of the Context.
     */
    val contextId: String
)