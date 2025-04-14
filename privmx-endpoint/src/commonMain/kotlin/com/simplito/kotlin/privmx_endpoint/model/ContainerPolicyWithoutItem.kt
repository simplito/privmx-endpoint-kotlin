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
 * Contains container's policies.
 *
 * @category core
 * @group Core
 */
open class ContainerPolicyWithoutItem
/**
 * Creates instance of [ContainerPolicyWithoutItem].
 *
 * @param get                             determines who can get a container
 * @param update                          determines who can update a container
 * @param delete                          determines who can delete a container
 * @param updatePolicy                    determines who can update policy
 * @param updaterCanBeRemovedFromManagers determines whether the updater can be removed from the list of managers
 * @param ownerCanBeRemovedFromManagers   determines whether the owner can be removed from the list of managers
 */(
    /**
     * Determines who can get a container.
     */
    val get: String?,
    /**
     * Determines who can update a container.
     */
    val update: String?,
    /**
     * Determines who can delete a container.
     */
    val delete: String?,
    /**
     * Determines who can update policy.
     */
    val updatePolicy: String?,
    /**
     * Determines whether the updater can be removed from the list of managers.
     */
    val updaterCanBeRemovedFromManagers: String?,
    /**
     * Determines whether the owner can be removed from the list of managers.
     */
    val ownerCanBeRemovedFromManagers: String?
)
