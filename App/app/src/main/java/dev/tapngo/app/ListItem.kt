package dev.tapngo.app

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import dev.tapngo.app.utils.inventreeutils.components.ItemListData
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun ListItem(itemListData: ItemListData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            bitmap = BitmapFactory.decodeByteArray(
                itemListData.thumbnail,
                0,
                itemListData.thumbnail.size
            ).asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "SKU: ${itemListData.sku}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = itemListData.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Stock: ${itemListData.quantity}", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
    }
}