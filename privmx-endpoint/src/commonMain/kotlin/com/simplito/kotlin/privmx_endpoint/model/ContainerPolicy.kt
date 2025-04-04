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
 * @category core
 * @group Core
 */
class ContainerPolicy(
    get: String?,
    update: String?,
    delete: String?,
    updatePolicy: String?,
    updaterCanBeRemovedFromManagers: String?,
    ownerCanBeRemovedFromManagers: String?,
    /**
     * Policy for container's items.
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
