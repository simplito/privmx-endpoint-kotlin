//
// PrivMX Endpoint Java Extra.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint_extra.policies

/**
 * Contains value for Container's item policies.
 */
open class ItemPolicyValue
/**
 * Creates instance of [ItemPolicyValue].
 *
 * @param value raw policy value
 */
internal constructor(override val value: String) : PolicyValue