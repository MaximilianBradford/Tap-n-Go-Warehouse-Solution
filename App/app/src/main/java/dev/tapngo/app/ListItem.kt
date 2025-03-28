package dev.tapngo.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tapngo.app.utils.inventreeutils.components.ItemListData

@Composable
fun ListItem(
    itemListData: ItemListData,
    onItemClick: (ItemListData) -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onItemClick(itemListData) }
            .fillMaxWidth()
            .padding(8.dp),

        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val bitmap = remember {
            runCatching {
                itemListData.thumbnail?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap()
                }
            }.getOrNull()
        }
        Image(
            painter = bitmap?.let { BitmapPainter(it) }
                ?: rememberVectorPainter(Icons.Filled.BrokenImage),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "SKU: ${itemListData.sku}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = itemListData.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Stock: ${itemListData.quantity}",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}