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
 * Contains container and its items policies.
 *
 * @param get                             Determines who can get a container
 * @param update                          Determines who can update a container
 * @param delete                          Determines who can delete a container
 * @param updatePolicy                    Determines who can update policy
 * @param updaterCanBeRemovedFromManagers Determines whether the updater can be removed from the list of managers
 * @param ownerCanBeRemovedFromManagers   Determines whether the owner can be removed from the list of managers
 * @property item                            Policy for container's items
 */
class ContainerPolicy(
    get: String?,
    update: String?,
    delete: String?,
    updatePolicy: String?,
    updaterCanBeRemovedFromManagers: String?,
    ownerCanBeRemovedFromManagers: String?,
    val item: ItemPolicy?
) : ContainerPolicyWithoutItem(
    get,
    update,
    delete,
    updatePolicy,
    updaterCanBeRemovedFromManagers,
    ownerCanBeRemovedFromManagers
)