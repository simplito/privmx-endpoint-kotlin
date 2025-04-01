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
package com.simplito.kotlin.privmx_endpoint_extra.policies

/**
 * Contains special policies values.
 * Creates instance of [SpecialPolicyValue].
 *
 * @param value raw policy value
 */
enum class SpecialPolicyValue(override val value: String) : PolicyValue {
    /**
     * Uses the default value provided by the Bridge.
     */
    DEFAULT("default"),

    /**
     * Uses the inherited value.
     */
    INHERIT("inherit"),

    /**
     * Allows to perform an action.
     */
    YES("yes"),

    /**
     * Denies to perform an action.
     */
    NO("no")
}