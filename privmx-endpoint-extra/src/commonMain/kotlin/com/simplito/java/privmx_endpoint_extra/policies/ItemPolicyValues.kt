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
 * Provides a set of predefined values for configuring item policies within a Container.
 */
object ItemPolicyValues {
    /**
     * Uses the default value provided by the Bridge.
     */
    val DEFAULT: ItemPolicyValue = ItemPolicyValue("default")

    /**
     * Uses the value inherited from the Context.
     */
    val INHERIT: ItemPolicyValue = ItemPolicyValue("inherit")

    /**
     * Prevents actions from being performed on item.
     */
    val NONE: ItemPolicyValue = ItemPolicyValue("none")

    /**
     * Allows all Context users to perform actions on item.
     */
    val ALL: ItemPolicyValue = ItemPolicyValue("all")

    /**
     * Allows all the container's users to perform actions on item.
     */
    val USER: ItemPolicyComplexValue = ItemPolicyComplexValue("user")

    /**
     * Allows the container's managers to perform actions on item.
     */
    val MANAGER: ItemPolicyComplexValue = ItemPolicyComplexValue("manager")

    /**
     * Allows the container's owner to perform actions on item.
     */
    val OWNER: ItemPolicyComplexValue = ItemPolicyComplexValue("owner")

    /**
     * Allows the item's owner to perform actions on item.
     */
    val ITEM_OWNER: ItemPolicyComplexValue = ItemPolicyComplexValue("itemOwner")
}
