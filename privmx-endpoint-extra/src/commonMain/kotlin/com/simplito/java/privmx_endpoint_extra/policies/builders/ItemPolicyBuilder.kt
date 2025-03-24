package com.simplito.java.privmx_endpoint_extra.policies.builders

import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.java.privmx_endpoint_extra.policies.ItemPolicyValue

interface ItemPolicyBuilderScope {
    fun get(policyValue: ItemPolicyValue): ItemPolicyBuilder
    fun listMy(policyValue: ContainerPolicyValue): ItemPolicyBuilder
    fun listAll(policyValue: ContainerPolicyValue): ItemPolicyBuilder
    fun create(policyValue: ContainerPolicyValue): ItemPolicyBuilder
    fun update(policyValue: ItemPolicyValue): ItemPolicyBuilder
    fun delete(policyValue: ItemPolicyValue): ItemPolicyBuilder
}

/**
 * Builder for creating instances of {@link ItemPolicy}.
 */
class ItemPolicyBuilder : ItemPolicyBuilderScope {
    private var get: String? = null
    private var listMy: String? = null
    private var listAll: String? = null
    private var create: String? = null
    private var update: String? = null
    private var delete: String? = null

    /**
     * Creates instance of {@link ItemPolicyBuilder} initialized with Bridge's default policy values.
     */
    constructor()

    /**
     * Creates instance of {@link ItemPolicyBuilder}
     * from existing {@link ItemPolicy} instance.
     *
     * @param itemPolicy the existing {@link ItemPolicy} instance to copy values from.
     */
    constructor(itemPolicy: ItemPolicy) : this() {
        this.get = itemPolicy.get
        this.listMy = itemPolicy.listMy
        this.listAll = itemPolicy.listAll
        this.create = itemPolicy.create
        this.update = itemPolicy.update
    }

    /**
     * Sets {@link ItemPolicy#get} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun get(policyValue: ItemPolicyValue) = apply { this.get = policyValue.value }

    /**
     * Sets {@link ItemPolicy#listMy} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun listMy(policyValue: ContainerPolicyValue) =
        apply { this.listMy = policyValue.value }

    /**
     * Sets {@link ItemPolicy#listAll} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun listAll(policyValue: ContainerPolicyValue) =
        apply { this.listAll = policyValue.value }

    /**
     * Sets {@link ItemPolicy#create} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun create(policyValue: ContainerPolicyValue) =
        apply { this.create = policyValue.value }

    /**
     * Sets {@link ItemPolicy#update} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun update(policyValue: ItemPolicyValue) = apply { this.update = policyValue.value }

    /**
     * Sets {@link ItemPolicy#delete} policy value.
     *
     * @param policyValue policy value to set
     * @return {@link ItemPolicyBuilder} instance to allow for method chaining.
     */
    override fun delete(policyValue: ItemPolicyValue) = apply { this.delete = policyValue.value }

    /**
     * Creates {@link ItemPolicy} from current state.
     *
     * @return new {@link ItemPolicy} instance created from this builder policies.
     */
    fun build() = ItemPolicy(
        get,
        listMy,
        listAll,
        create,
        update,
        delete
    )
}

fun itemPolicy(
    itemPolicy: ItemPolicy? = null,
    buildBlock: ItemPolicyBuilderScope.() -> Unit
): ItemPolicy {
    return if (itemPolicy == null) {
        ItemPolicyBuilder().apply(buildBlock).build()
    } else {
        ItemPolicyBuilder(itemPolicy).apply(buildBlock).build()
    }
}