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
package com.simplito.java.privmx_endpoint_extra.policies

/**
 * Contains special policies values.
 */
class SpecialPolicyValue
/**
 * Creates instance of [SpecialPolicyValue].
 *
 * @param value raw policy value
 */
internal constructor(value: String) : PolicyValue(value) {
    companion object {
        /**
         * Uses the default value provided by the Bridge.
         */
        val DEFAULT: SpecialPolicyValue = SpecialPolicyValue("default")

        /**
         * Uses the inherited value.
         */
        val INHERIT: SpecialPolicyValue = SpecialPolicyValue("inherit")

        /**
         * Allows to perform an action.
         */
        val YES: SpecialPolicyValue = SpecialPolicyValue("yes")

        /**
         * Denies to perform an action.
         */
        val NO: SpecialPolicyValue = SpecialPolicyValue("no")
    }
}
