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

import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.kotlin.privmx_endpoint_extra.policies.ItemPolicyValue

/**
 * Scope for creating [ItemPolicy].
 */
interface ItemPolicyBuilderScope {
    /**
     * Sets [ItemPolicy.get] policy value for reading (getting) an item.
     *
     * @param policyValue the rule determining who can read an item
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun get(policyValue: ItemPolicyValue): ItemPolicyBuilderScope

    /**
     * Sets [ItemPolicy.listMy] policy value for listing items owned by the current user.
     *
     * @param policyValue the rule determining who can list their own items
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun listMy(policyValue: ContainerPolicyValue): ItemPolicyBuilderScope

    /**
     * Sets [ItemPolicy.listAll] policy value for listing all items in the container.
     *
     * @param policyValue the rule determining who can list all items
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun listAll(policyValue: ContainerPolicyValue): ItemPolicyBuilderScope

    /**
     * Sets [ItemPolicy.create] policy value for creating new items in the container.
     *
     * @param policyValue the rule determining who can create items
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun create(policyValue: ContainerPolicyValue): ItemPolicyBuilderScope

    /**
     * Sets [ItemPolicy.update] policy value for updating items.
     *
     * @param policyValue the rule determining who can modify items
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun update(policyValue: ItemPolicyValue): ItemPolicyBuilderScope

    /**
     * Sets [ItemPolicy.delete] policy value for deleting items.
     *
     * @param policyValue the rule determining who can delete items
     * @return [ItemPolicyBuilderScope] instance to allow for method chaining
     */
    fun delete(policyValue: ItemPolicyValue): ItemPolicyBuilderScope
}

/**
 * Builder for creating instances of [ItemPolicy].
 */
class ItemPolicyBuilder : ItemPolicyBuilderScope {
    private var get: String? = null
    private var listMy: String? = null
    private var listAll: String? = null
    private var create: String? = null
    private var update: String? = null
    private var delete: String? = null

    /**
     * Initializes [ItemPolicyBuilder] with Bridge's default policy values.
     */
    constructor()

    /**
     * Initializes [ItemPolicyBuilder] from existing [ItemPolicy] instance.
     *
     * @param itemPolicy the existing [ItemPolicy] instance to copy values from
     */
    constructor(itemPolicy: ItemPolicy) : this() {
        this.get = itemPolicy.get
        this.listMy = itemPolicy.listMy
        this.listAll = itemPolicy.listAll
        this.create = itemPolicy.create
        this.update = itemPolicy.update
        this.delete = itemPolicy.delete
    }

    /**
     * Sets [ItemPolicy.get] policy value for reading (getting) an item.
     *
     * @param policyValue the rule determining who can read an item
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun get(policyValue: ItemPolicyValue): ItemPolicyBuilder =
        apply { this.get = policyValue.value }

    /**
     * Sets [ItemPolicy.listMy] policy value for listing items owned by the current user.
     *
     * @param policyValue the rule determining who can list their own items
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun listMy(policyValue: ContainerPolicyValue): ItemPolicyBuilder =
        apply { this.listMy = policyValue.value }

    /**
     * Sets [ItemPolicy.listAll] policy value for listing all items in the container.
     *
     * @param policyValue the rule determining who can list all items
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun listAll(policyValue: ContainerPolicyValue): ItemPolicyBuilder =
        apply { this.listAll = policyValue.value }

    /**
     * Sets [ItemPolicy.create] policy value for creating new items in the container.
     *
     * @param policyValue the rule determining who can create items
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun create(policyValue: ContainerPolicyValue): ItemPolicyBuilder =
        apply { this.create = policyValue.value }

    /**
     * Sets [ItemPolicy.update] policy value for updating items.
     *
     * @param policyValue the rule determining who can modify items
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun update(policyValue: ItemPolicyValue): ItemPolicyBuilder =
        apply { this.update = policyValue.value }

    /**
     * Sets [ItemPolicy.delete] policy value for deleting items.
     *
     * @param policyValue the rule determining who can delete items
     * @return [ItemPolicyBuilder] instance to allow for method chaining
     */
    override fun delete(policyValue: ItemPolicyValue): ItemPolicyBuilder =
        apply { this.delete = policyValue.value }

    /**
     * Creates [ItemPolicy] from current state.
     *
     * @return new [ItemPolicy] instance created from this builder policies
     */
    fun build() = ItemPolicy(
        get, listMy, listAll, create, update, delete
    )
}

/**
 * Builds an [ItemPolicy] using a DSL builder block.
 *
 * This function allows building a new [ItemPolicy].
 * If a [currentPolicy] is provided, its settings will be used as a base.
 *
 * ### Example:
 * ```
 * val policy = itemPolicy {
 *     get(ItemPolicyValues.DEFAULT)
 *     create(...)
 *     ...
 * }
 * ```
 *
 * @param currentPolicy optional base policy to build upon; if `null`, a new builder is created
 * @param buildBlock the block that defines item-level access rules within [ItemPolicyBuilderScope]
 * @return the resulting [ItemPolicy] after applying the builder block
 */
fun itemPolicy(
    currentPolicy: ItemPolicy? = null,
    buildBlock: ItemPolicyBuilderScope.() -> Unit
) = currentPolicy.builder().apply(buildBlock).build()

private fun ItemPolicy?.builder() = this?.let { ItemPolicyBuilder(it) } ?: ItemPolicyBuilder()