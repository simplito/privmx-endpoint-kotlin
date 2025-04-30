package Stacks.Kotlin

import com.simplito.java.privmx_endpoint.model.ContainerPolicy
import com.simplito.java.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.java.privmx_endpoint.model.ItemPolicy
import com.simplito.java.privmx_endpoint_extra.policies.ContainerPolicyValues
import com.simplito.java.privmx_endpoint_extra.policies.ItemPolicyValues
import com.simplito.java.privmx_endpoint_extra.policies.builders.ContainerPolicyBuilder
import com.simplito.java.privmx_endpoint_extra.policies.builders.ItemPolicyBuilder

fun buildItemPolicy() {
    val itemPolicy: ItemPolicy = ItemPolicyBuilder()
        .setUpdate(
            ItemPolicyValues.ITEM_OWNER
                .AND(ItemPolicyValues.MANAGER)
                .OR(ItemPolicyValues.USER)
        )
        .setListMy(ContainerPolicyValues.USER)
        .build()
}

fun buildContainerWithoutItemPolicy() {
    val containerPolicyWithoutItem: ContainerPolicyWithoutItem = ContainerPolicyBuilder()
        .setGet(ContainerPolicyValues.ALL)
        .setUpdatePolicy(
            ContainerPolicyValues.OWNER
                .AND(ContainerPolicyValues.MANAGER)
        )
        .buildWithoutItem()
}

fun buildContainerPolicy() {
    val containerPolicy: ContainerPolicy = ContainerPolicyBuilder()
        .setGet(ContainerPolicyValues.ALL)
        .setItem(
            ItemPolicyBuilder()
                .setUpdate(
                    ItemPolicyValues.ITEM_OWNER
                        .AND(ItemPolicyValues.MANAGER)
                        .OR(ItemPolicyValues.USER)
                )
                .setListMy(ContainerPolicyValues.USER)
                .build()
        )
        .build()
}
