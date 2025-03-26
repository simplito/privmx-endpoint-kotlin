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
 * Provides a set of predefined values for configuring container policies.
 */
object ContainerPolicyValues {
    /**
     * Uses the default value provided by the Bridge.
     */
    val DEFAULT: ContainerPolicyValue = ContainerPolicyValue("default")

    /**
     * Uses the value inherited from the Context.
     */
    val INHERIT: ContainerPolicyValue = ContainerPolicyValue("inherit")

    /**
     * Prevents actions from being performed on container.
     */
    val NONE: ContainerPolicyValue = ContainerPolicyValue("none")

    /**
     * Allows all Context users to perform actions on container.
     */
    val ALL: ContainerPolicyValue = ContainerPolicyValue("all")

    /**
     * Allows the container's users to perform actions on container.
     */
    val USER: ContainerPolicyComplexValue = ContainerPolicyComplexValue("user")

    /**
     * Allows the container's managers to perform actions on container.
     */
    val MANAGER: ContainerPolicyComplexValue = ContainerPolicyComplexValue("manager")

    /**
     * Allows the container's owner to perform actions on container.
     */
    val OWNER: ContainerPolicyComplexValue = ContainerPolicyComplexValue("owner")
}
