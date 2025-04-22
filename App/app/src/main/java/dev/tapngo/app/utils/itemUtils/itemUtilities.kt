package dev.tapngo.app.utils.itemUtils

import android.util.Log
import dev.tapngo.app.utils.inventreeutils.components.ItemData

private var item: ItemData = ItemData(
    id = -1,
    loc = null
)

fun updateItem(itemData: ItemData) {
    item = itemData
}

fun getItem(): ItemData {
    try {
        return item
    } catch (e: Exception) {
        Log.d("itemFunction","Issue retrieving current item: $item. Error: $item")
        return ItemData(
            id = -1,
            loc = null
        )
    }
}