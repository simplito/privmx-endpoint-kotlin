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
package com.simplito.kotlin.privmx_endpoint_extra.policies.builders

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.kotlin.privmx_endpoint_extra.policies.SpecialPolicyValue

/**
 * Scope for creating [ContainerPolicyWithoutItem].
 */
interface ContainerPolicyWithoutItemBuilderScope {
    /**
     * Sets [ContainerPolicyWithoutItem.get] policy value
     *
     * @param policyValue the rule determining who can get container
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun get(policyValue: ContainerPolicyValue): ContainerPolicyBuilder

    /**
     * ets [ContainerPolicyWithoutItem.update] policy value.
     *
     * @param policyValue the rule determining who can update container
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun update(policyValue: ContainerPolicyValue): ContainerPolicyBuilder

    /**
     * Sets [ContainerPolicyWithoutItem.delete] policy value.
     *
     * @param policyValue the rule determining who can delete container
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun delete(policyValue: ContainerPolicyValue): ContainerPolicyBuilder

    /**
     * Sets [ContainerPolicyWithoutItem.updatePolicy] policy value.
     *
     * @param policyValue the access rule for modifying container policies
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun updatePolicy(policyValue: ContainerPolicyValue): ContainerPolicyBuilder

    /**
     * Sets [ContainerPolicyWithoutItem.updaterCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue the special rule indicating if user who can update the container can be removed from the list of managers.
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder

    /**
     * Sets [ContainerPolicyWithoutItem.ownerCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue the special rule indicating if removal is allowed
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder
}

/**
 * Scope for creating [ContainerPolicy].
 */
interface ContainerPolicyBuilderScope : ContainerPolicyWithoutItemBuilderScope {
    /**
     * Sets the access policy for items within the container.
     *
     * @param item the item access policy
     * @return  [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    fun item(item: ItemPolicy): ContainerPolicyBuilder
}

/**
 * Builder for creating instances of [ContainerPolicyWithoutItem] and [ContainerPolicy].
 */
class ContainerPolicyBuilder : ContainerPolicyBuilderScope {
    private var get: String? = null
    private var update: String? = null
    private var delete: String? = null
    private var updatePolicy: String? = null
    private var updaterCanBeRemovedFromManagers: String? = null
    private var ownerCanBeRemovedFromManagers: String? = null
    private var item: ItemPolicy? = null

    /**
     * Creates instance of [ContainerPolicyBuilder] initialized with Bridge's default policy values.
     */
    constructor()

    /**
     * Creates instance of [ContainerPolicyBuilder]
     * initialized with policy values from existing [ContainerPolicy] instance.
     *
     * @param containerPolicy the existing [ContainerPolicy] instance to copy values from.
     */
    constructor(containerPolicy: ContainerPolicy) {
        this.get = containerPolicy.get
        this.update = containerPolicy.update
        this.delete = containerPolicy.delete
        this.updatePolicy = containerPolicy.updatePolicy
        this.updaterCanBeRemovedFromManagers = containerPolicy.updaterCanBeRemovedFromManagers
        this.ownerCanBeRemovedFromManagers = containerPolicy.ownerCanBeRemovedFromManagers
        this.item = containerPolicy.item
    }

    /**
     * Creates instance of [ContainerPolicyBuilder]
     * initialized with policy values from existing [ContainerPolicyWithoutItem] instance.
     *
     * @param containerPolicyWithoutItem the existing [ContainerPolicyWithoutItem] instance to copy values from.
     */
    constructor(containerPolicyWithoutItem: ContainerPolicyWithoutItem) {
        this.get = containerPolicyWithoutItem.get
        this.update = containerPolicyWithoutItem.update
        this.delete = containerPolicyWithoutItem.delete
        this.updatePolicy = containerPolicyWithoutItem.updatePolicy
        this.updaterCanBeRemovedFromManagers =
            containerPolicyWithoutItem.updaterCanBeRemovedFromManagers
        this.ownerCanBeRemovedFromManagers =
            containerPolicyWithoutItem.ownerCanBeRemovedFromManagers
    }

    override fun get(policyValue: ContainerPolicyValue) = apply { this.get = policyValue.value }

    override fun update(policyValue: ContainerPolicyValue) =
        apply { this.update = policyValue.value }

    override fun delete(policyValue: ContainerPolicyValue) =
        apply { this.delete = policyValue.value }

    override fun updatePolicy(policyValue: ContainerPolicyValue) =
        apply { this.updatePolicy = policyValue.value }

    override fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue) =
        apply { this.updaterCanBeRemovedFromManagers = policyValue.value }

    override fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue) =
        apply { this.ownerCanBeRemovedFromManagers = policyValue.value }

    override fun item(item: ItemPolicy) = apply { this.item = item }

    /**
     * Creates [ContainerPolicyWithoutItem] from current state.
     *
     * @return new [ContainerPolicyWithoutItem] instance created from this builder policies.
     */
    fun buildWithoutItem() = ContainerPolicyWithoutItem(
        get,
        update,
        delete,
        updatePolicy,
        updaterCanBeRemovedFromManagers,
        ownerCanBeRemovedFromManagers
    )

    /**
     * Creates [ContainerPolicy] from current state.
     *
     * @return new [ContainerPolicy] instance created from this builder policies.
     */
    fun build() = ContainerPolicy(
        get,
        update,
        delete,
        updatePolicy,
        updaterCanBeRemovedFromManagers,
        ownerCanBeRemovedFromManagers,
        item
    )
}

/**
 * Creates or updates a [ContainerPolicy] using a DSL builder.
 * This function allows building a full container policy, including item-level access rules.
 * If a [currentPolicy] is provided, its settings will be used as a base.
 *
 * ### Example:
 * ```
 * val policy = containerPolicy {
 *     get(ContainerPolicyValues.DEFAULT)
 *     item(ItemPolicy(...))
 *     update(...)
 * }
 * ```
 *
 * @param currentPolicy optional existing policy to use as a base
 * @param buildBlock container-level policy configuration
 * @return [ContainerPolicy]
 */
fun containerPolicy(
    currentPolicy: ContainerPolicy? = null, buildBlock: ContainerPolicyBuilderScope.() -> Unit
) = currentPolicy.builder().apply(buildBlock).build()

/**
 * Creates or updates a [ContainerPolicyWithoutItem] using a DSL builder.
 *
 * This function allows building container-level policies only.
 * If a [currentPolicy] is provided, its settings will be used as a base.
 *
 * ### Example:
 * ```
 * val policy = containerPolicyWithoutItem {
 *     get(ContainerPolicyValues.DEFAULT)
 *     update(...)
 * }
 * ```
 *
 * @param currentPolicy optional existing policy to use as a base
 * @param buildBlock container-level policy configuration
 * @return [ContainerPolicyWithoutItem]
 */
fun containerPolicyWithoutItem(
    currentPolicy: ContainerPolicyWithoutItem? = null,
    buildBlock: ContainerPolicyWithoutItemBuilderScope.() -> Unit
) = currentPolicy.builder().apply(buildBlock).buildWithoutItem()

private fun <T : ContainerPolicyWithoutItem> T?.builder() =
    this?.let { ContainerPolicyBuilder(it) } ?: ContainerPolicyBuilder()