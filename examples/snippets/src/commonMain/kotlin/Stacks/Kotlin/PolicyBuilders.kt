package Stacks.Kotlin

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint_extra.policies.ContainerPolicyValues
import com.simplito.kotlin.privmx_endpoint_extra.policies.ItemPolicyValues
import com.simplito.kotlin.privmx_endpoint_extra.policies.builders.ContainerPolicyBuilder
import com.simplito.kotlin.privmx_endpoint_extra.policies.builders.ItemPolicyBuilder

fun buildItemPolicy() {
    val itemPolicy: ItemPolicy = ItemPolicyBuilder()
        .update(
            ItemPolicyValues.ITEM_OWNER
                .AND(ItemPolicyValues.MANAGER)
                .OR(ItemPolicyValues.USER)
        )
        .listMy(ContainerPolicyValues.USER)
        .build()
}

fun buildContainerWithoutItemPolicy() {
    val containerPolicyWithoutItem: ContainerPolicyWithoutItem = ContainerPolicyBuilder()
        .get(ContainerPolicyValues.ALL)
        .updatePolicy(
            ContainerPolicyValues.OWNER
                .AND(ContainerPolicyValues.MANAGER)
        )
        .buildWithoutItem()
}

fun buildContainerPolicy() {
    val containerPolicy: ContainerPolicy = ContainerPolicyBuilder()
        .get(ContainerPolicyValues.ALL)
        .item(
            ItemPolicyBuilder()
                .update(
                    ItemPolicyValues.ITEM_OWNER
                        .AND(ItemPolicyValues.MANAGER)
                        .OR(ItemPolicyValues.USER)
                )
                .listMy(ContainerPolicyValues.USER)
                .build()
        )
        .build()
}
