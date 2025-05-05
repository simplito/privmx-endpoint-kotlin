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
 *
 * @property userId        ID of the user requesting information
 * @property contextId     ID of the Context
 */
data class Context(val userId: String, val contextId: String)