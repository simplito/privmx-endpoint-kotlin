//
// PrivMX Endpoint Java.
// Copyright © 2024 Simplito sp. z o.o.
//
// This file is part of the PrivMX Platform (https://privmx.dev).
// This software is Licensed under the MIT License.
//
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.simplito.kotlin.privmx_endpoint.model

/**
 * Contains container and its items policies.
 *
 * @param get                             determines who can get a container
 * @param update                          determines who can update a container
 * @param delete                          determines who can delete a container
 * @param updatePolicy                    determines who can update policy
 * @param updaterCanBeRemovedFromManagers determines whether the updater can be removed from the list of managers
 * @param ownerCanBeRemovedFromManagers   determines whether the owner can be removed from the list of managers
 * @param item                            policy for container's items
 *
 * @category core
 * @group Core
 */
class ContainerPolicy(
    /**
     * Determines who can get a container
     */
    get: String?,
    /**
     * Determines who can update a container
     */
    update: String?,
    /**
     * determines who can delete a container
     */
    delete: String?,
    /**
     * Determines who can update policy
     */
    updatePolicy: String?,
    /**
     * Determines whether the updater can be removed from the list of managers
     */
    updaterCanBeRemovedFromManagers: String?,
    /**
     * Determines whether the owner can be removed from the list of managers
     */
    ownerCanBeRemovedFromManagers: String?,
    /**
     * Policy for container's items
     */
    val item: ItemPolicy?
) : ContainerPolicyWithoutItem(
    get,
    update,
    delete,
    updatePolicy,
    updaterCanBeRemovedFromManagers,
    ownerCanBeRemovedFromManagers
)