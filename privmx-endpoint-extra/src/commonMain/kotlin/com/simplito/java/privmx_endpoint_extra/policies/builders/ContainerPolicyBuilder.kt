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
import com.simplito.java.privmx_endpoint_extra.policies.SpecialPolicyValue
import kotlin.math.cbrt

/**
 * Builder for creating instances of [ContainerPolicyWithoutItem] and [ContainerPolicy].
 */
class ContainerPolicyBuilder {
    internal var get: String? = null
    internal var update: String? = null
    internal var delete: String? = null
    internal var updatePolicy: String? = null
    internal var updaterCanBeRemovedFromManagers: String? = null
    internal var ownerCanBeRemovedFromManagers: String? = null
    internal var item: ItemPolicy? = null

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

//    fun ContainerPolicyBuilder(containerPolicy: ContainerPolicyBuilder.() -> Unit): ContainerPolicyWithoutItem
//    {
//        return ContainerPolicyBuilder().apply(containerPolicy).buildWithoutItem()
//    }

    fun setContainerPolicy(containerPolicy: ContainerPolicyBuilder.() -> Unit): ContainerPolicy =
        ContainerPolicyBuilder().apply(containerPolicy).build()


    fun setContainerPolicyWithoutItem(containerPolicy: ContainerPolicyBuilder.() -> Unit): ContainerPolicyWithoutItem =
        ContainerPolicyBuilder().apply(containerPolicy).buildWithoutItem()



    /**
     * Sets [ContainerPolicyWithoutItem.get] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun get(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.get = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.update] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun update(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.update = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.delete] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun delete(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.delete = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.updatePolicy] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun updatePolicy(policyValue: ContainerPolicyValue): ContainerPolicyBuilder {
        this.updatePolicy = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.updaterCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun updaterCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder {
        this.updaterCanBeRemovedFromManagers = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicyWithoutItem.ownerCanBeRemovedFromManagers] policy value.
     *
     * @param policyValue policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun ownerCanBeRemovedFromManagers(policyValue: SpecialPolicyValue): ContainerPolicyBuilder {
        this.ownerCanBeRemovedFromManagers = policyValue.value
        return this
    }

    /**
     * Sets [ContainerPolicy.item] items policy value.
     *
     * @param item policy value to set
     * @return [ContainerPolicyBuilder] instance to allow for method chaining.
     */
    infix fun item(item: ItemPolicy?): ContainerPolicyBuilder {
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

fun ContainerPolicy.Companion.build(block: ContainerPolicyBuilder.() -> Unit): ContainerPolicy =
    ContainerPolicyBuilder().apply(block).build()

fun ContainerPolicyWithoutItem.Companion.build(block: ContainerPolicyBuilder.() -> Unit): ContainerPolicyWithoutItem =
    ContainerPolicyBuilder().apply(block).buildWithoutItem()


fun ContainerPolicy.update(block: ContainerPolicyBuilder.() -> Unit): ContainerPolicy =
    ContainerPolicyBuilder(this).apply(block).build()

fun ContainerPolicyWithoutItem.update(block: ContainerPolicyBuilder.() -> Unit): ContainerPolicyWithoutItem =
    ContainerPolicyBuilder(this).apply(block).buildWithoutItem()





















