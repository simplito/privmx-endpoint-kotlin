package Stacks.Kotlin

import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicy
import com.simplito.kotlin.privmx_endpoint.model.ContainerPolicyWithoutItem
import com.simplito.kotlin.privmx_endpoint.model.ItemPolicy
import com.simplito.kotlin.privmx_endpoint_extra.policies.ContainerPolicyValues
import com.simplito.kotlin.privmx_endpoint_extra.policies.ItemPolicyValue
import com.simplito.kotlin.privmx_endpoint_extra.policies.ItemPolicyValues
import com.simplito.kotlin.privmx_endpoint_extra.policies.builders.*

fun combiningPolicyValues() {
    val itemPolicyValue: ItemPolicyValue = ItemPolicyValues.ITEM_OWNER AND
            ItemPolicyValues.MANAGER OR
            ItemPolicyValues.USER
}

fun buildItemPolicy() {
    val itemPolicy: ItemPolicy = ItemPolicyBuilder()
        .update(
            ItemPolicyValues.ITEM_OWNER AND
                    ItemPolicyValues.MANAGER OR
                    ItemPolicyValues.USER
        )
        .listMy(ContainerPolicyValues.USER)
        .build()
}

fun buildContainerWithoutItemPolicy() {
    val containerPolicyWithoutItem: ContainerPolicyWithoutItem = ContainerPolicyBuilder()
        .get(ContainerPolicyValues.ALL)
        .updatePolicy(
            ContainerPolicyValues.OWNER AND
                    ContainerPolicyValues.MANAGER
        )
        .buildWithoutItem()
}

fun buildContainerPolicy() {
    val containerPolicy: ContainerPolicy = ContainerPolicyBuilder()
        .get(ContainerPolicyValues.ALL)
        .item(
            ItemPolicyBuilder()
                .update(
                    ItemPolicyValues.ITEM_OWNER AND
                            ItemPolicyValues.MANAGER OR
                            ItemPolicyValues.USER
                )
                .listMy(ContainerPolicyValues.USER)
                .build()
        )
        .build()
}

fun buildItemPolicyUsingHelpers() {
    val itemPolicy: ItemPolicy = itemPolicy {
        update(
            ItemPolicyValues.ITEM_OWNER AND
                    ItemPolicyValues.MANAGER OR
                    ItemPolicyValues.USER
        )
        listMy(ContainerPolicyValues.USER)
    }
}

fun buildContainerWithoutItemPolicyUsingHelpers() {
    val containerPolicyWithoutItem: ContainerPolicyWithoutItem = containerPolicyWithoutItem {
        get(ContainerPolicyValues.ALL)
        updatePolicy(
            ContainerPolicyValues.OWNER AND
                    ContainerPolicyValues.MANAGER
        )
    }
}

fun buildContainerPolicyUsingHelpers() {
    val containerPolicy: ContainerPolicy = containerPolicy {
        get(ContainerPolicyValues.ALL)
        item(
            itemPolicy {
                update(
                    ItemPolicyValues.ITEM_OWNER AND
                            ItemPolicyValues.MANAGER OR
                            ItemPolicyValues.USER
                )
                listMy(ContainerPolicyValues.USER)
            }
        )
    }
}