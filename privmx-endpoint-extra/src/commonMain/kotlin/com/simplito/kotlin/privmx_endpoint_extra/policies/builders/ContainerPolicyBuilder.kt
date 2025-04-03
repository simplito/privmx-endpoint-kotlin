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
package com.simplito.kotlin.privmx_endpoint_extra.policies.builders

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.kotlin.privmx_endpoint_extra.policies.SpecialPolicyValue

interface ContainerPolicyWithoutItemBuilderScope {
    fun get(policyValue: ContainerPolicyValue): ContainerPolicyBuilder
    fun update(policyValue: ContainerPolicyValue): ContainerPolicyBuilder
    fun delete(policyValue: ContainerPolicyValue): ContainerPolicyBuilder
    fun updatePolicy(policyValue: ContainerPolicyValue): ContainerPolicyBuilder
    fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder
    fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder
}

interface ContainerPolicyBuilderScope : ContainerPolicyWithoutItemBuilderScope {
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

    /**
     * Sets [ContainerPolicyWithoutItem.get] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun get(policyValue: ContainerPolicyValue) = apply { this.get = policyValue.value }

    /**
     * Sets [ContainerPolicyWithoutItem.update] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun update(policyValue: ContainerPolicyValue) =
        apply { this.update = policyValue.value }

    /**
     * Sets [ContainerPolicyWithoutItem.delete] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun delete(policyValue: ContainerPolicyValue) =
        apply { this.delete = policyValue.value }

    /**
     * Sets [ContainerPolicyWithoutItem.updatePolicy] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun updatePolicy(policyValue: ContainerPolicyValue) =
        apply { this.updatePolicy = policyValue.value }

    /**
     * Sets [ContainerPolicyWithoutItem.updaterCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue) =
        apply { this.updaterCanBeRemovedFromManagers = policyValue.value }

    /**
     * Sets [ContainerPolicyWithoutItem.ownerCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue) =
        apply { this.ownerCanBeRemovedFromManagers = policyValue.value }

    /**
     * Sets [ContainerPolicy.item] items policy value.
     *
     * @param item policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
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

fun containerPolicy(
    currentPolicy: ContainerPolicy? = null,
    buildBlock: ContainerPolicyBuilderScope.() -> Unit
) = currentPolicy.builder().apply(buildBlock).build()

fun containerPolicyWithoutItem(
    currentPolicy: ContainerPolicyWithoutItem? = null,
    buildBlock: ContainerPolicyWithoutItemBuilderScope.() -> Unit
) = currentPolicy.builder().apply(buildBlock).buildWithoutItem()

private fun <T : ContainerPolicyWithoutItem> T?.builder() =
    this?.let { ContainerPolicyBuilder(it) } ?: ContainerPolicyBuilder()