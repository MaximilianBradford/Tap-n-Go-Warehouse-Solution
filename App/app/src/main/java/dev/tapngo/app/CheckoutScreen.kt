package dev.tapngo.app

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.tapngo.app.ui.theme.TapNGoTheme
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


// Checkout screen component
@Composable
fun CheckoutScreen(id: Int) {
    var itemData by remember { mutableStateOf<ItemData?>(null) }

    LaunchedEffect(id) {
        itemData = withContext(Dispatchers.IO) {
            InvenTreeUtils.getItemData(id)
        }
    }

    TapNGoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Checkout", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            itemData?.let {
                Text(text = "SKU: ${it.sku}", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Description: ${it.description}", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                if (it.imageData != null) {
                    Image(
                        bitmap = BitmapFactory.decodeByteArray(it.imageData, 0, it.imageData!!.size).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(128.dp)
                    )
                }
            }
        }
    }
}