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
 * Contains container's policies.
 *
 * @property get                             Determines who can get a container.
 * @property update                          Determines who can update a container.
 * @property delete                          Determines who can delete a container.
 * @property updatePolicy                    Determines who can update policy.
 * @property updaterCanBeRemovedFromManagers Determines whether the updater can be removed from the list of managers.
 * @property ownerCanBeRemovedFromManagers   Determines whether the owner can be removed from the list of managers.
 *
 * @category core
 * @group Core
 */
open class ContainerPolicyWithoutItem(
    val get: String?,
    val update: String?,
    val delete: String?,
    val updatePolicy: String?,
    val updaterCanBeRemovedFromManagers: String?,
    val ownerCanBeRemovedFromManagers: String?
)