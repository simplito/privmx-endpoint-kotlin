package com.simplito.java.privmx_endpoint_extra.policies.builders

import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint.modules.core.Connection
import com.simplito.java.privmx_endpoint.modules.thread.ThreadApi
import com.simplito.java.privmx_endpoint_extra.policies.ContainerPolicyValue
import com.simplito.java.privmx_endpoint_extra.policies.ItemPolicyValue
import com.simplito.java.privmx_endpoint_extra.policies.ItemPolicyValues
import com.simplito.java.privmx_endpoint_extra.policies.PolicyValue

class ItemPolicyBuilder() {

    private var get: String? = null
    internal var listMy: String? = null
    internal var listAll: String? = null
    internal var create: String? = null
    internal var update: String? = null
    internal var delete: String? = null


    constructor(itemPolicy: ItemPolicy): this() {
        this.get = itemPolicy.get
        this.listMy = itemPolicy.listMy
        this.listAll = itemPolicy.listAll
        this.create = itemPolicy.create
        this.update = itemPolicy.update
    }

    fun setItemPolicy(itemPolicy: ItemPolicyBuilder.() -> Unit) = ItemPolicyBuilder().apply(itemPolicy).build()


    infix fun get(policyValue: ItemPolicyValue): ItemPolicyBuilder {
        this.get = policyValue.value
        return this
    }

    infix fun listMy(policyValue: ContainerPolicyValue): ItemPolicyBuilder {
        this.listMy = policyValue.value
        return this
    }

    infix fun listAll(policyValue: ContainerPolicyValue): ItemPolicyBuilder {
        this.listAll = policyValue.value
        return this
    }

    infix fun create(policyValue: ContainerPolicyValue): ItemPolicyBuilder {
        this.create = policyValue.value
        return this
    }

    infix fun update(policyValue: ItemPolicyValue): ItemPolicyBuilder {
        this.update = policyValue.value
        return this
    }

    infix fun delete(policyValue: ItemPolicyValue): ItemPolicyBuilder {
        this.delete = policyValue.value
        return this
    }

     fun build(): ItemPolicy {
        return ItemPolicy(
            get,
            listMy,
            listAll,
            create,
            update,
            delete
        )
    }
}


fun ItemPolicy.Companion.build(block: ItemPolicyBuilder.()->Unit): ItemPolicy = ItemPolicyBuilder().apply(block).build()
fun ItemPolicy.update(block: ItemPolicyBuilder.()->Unit): ItemPolicy = ItemPolicyBuilder(this).apply(block).build()



//fun test(){
//    val itemPolicy = ItemPolicy.build{
//        get(ItemPolicyValues.ITEM_OWNER AND (ItemPolicyValues.MANAGER OR ItemPolicyValues.USER))
//        get(ItemPolicyValues.ITEM_OWNER AND (ItemPolicyValues.MANAGER OR ItemPolicyValues.USER))
//    }
//
//
//    ItemPolicyBuilder(itemPolicy)
//    itemPolicy.updateItem {
//        get (ItemPolicyValues.ITEM_OWNER AND (ItemPolicyValues.MANAGER OR ItemPolicyValues.USER))
//    }
//
//}






























