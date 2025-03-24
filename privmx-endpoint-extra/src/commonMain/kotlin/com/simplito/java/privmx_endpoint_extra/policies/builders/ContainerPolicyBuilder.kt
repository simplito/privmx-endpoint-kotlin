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
package com.simplito.java.privmx_endpoint_extra.policies.builders

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.java.privmx_endpoint_extra.policies.ContainerPolicyValues
import com.simplito.java.privmx_endpoint_extra.policies.SpecialPolicyValue

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

    companion object {
        fun containerPolicy(
            containerPolicy: ContainerPolicy? = null,
            buildBlock: ContainerPolicyBuilderScope.() -> Unit
        ): ContainerPolicy {
            if (containerPolicy == null) {
                return ContainerPolicyBuilder().apply(buildBlock).build()
            } else {
                return ContainerPolicyBuilder(containerPolicy).apply(buildBlock).build()
            }
        }


        fun containerPolicyWithoutItem(
            containerPolicyWithoutItem: ContainerPolicyWithoutItem? = null,
            buildBlock: ContainerPolicyWithoutItemBuilderScope.() -> Unit
        ): ContainerPolicyWithoutItem =
            ContainerPolicyBuilder().apply(buildBlock).buildWithoutItem()
    }

    /**
     * Sets [ContainerPolicyWithoutItem.get] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun get(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.get = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.update] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun update(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.update = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.delete] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun delete(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.delete = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.updatePolicy] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun updatePolicy(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.updatePolicy = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.updaterCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder {
        this.updaterCanBeRemovedFromManagers = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.ownerCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder {
        this.ownerCanBeRemovedFromManagers = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicy.item] items policy value.
     *
     * @param item policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    override fun item(item: ItemPolicy): ContainerPolicyBuilder {
        this.item = item
        return this
    }

    /**
     * Creates [ContainerPolicyWithoutItem] from current state.
     *
     * @return new [ContainerPolicyWithoutItem] instance created from this builder policies.
     */
    fun buildWithoutItem(): ContainerPolicyWithoutItem {
        return ContainerPolicyWithoutItem(
            get,
            update,
            delete,
            updatePolicy,
            updaterCanBeRemovedFromManagers,
            ownerCanBeRemovedFromManagers
        )
    }

    /**
     * Creates [ContainerPolicy] from current state.
     *
     * @return new [ContainerPolicy] instance created from this builder policies.
     */
    fun build(): ContainerPolicy {
        return ContainerPolicy(
            get,
            update,
            delete,
            updatePolicy,
            updaterCanBeRemovedFromManagers,
            ownerCanBeRemovedFromManagers,
            item
        )
    }
}
