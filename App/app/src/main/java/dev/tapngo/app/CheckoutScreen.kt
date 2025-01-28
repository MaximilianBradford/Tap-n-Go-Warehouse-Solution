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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import dev.tapngo.app.ui.theme.TapNGoTheme
import dev.tapngo.app.utils.inventreeutils.components.ItemData


// Checkout screen component
@Composable
fun CheckoutScreen(itemData: ItemData) {
    TapNGoTheme {
        // I don't even center this one
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Checkout", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            // It's safe to assume that the itemData is not null
            // Even if it is, theoretically the following values will just simply not appear.
            Text(text = "SKU: ${itemData.sku}", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Description: ${itemData.description}", color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            if (itemData.imageData != null) {
                Image(
                    bitmap = BitmapFactory.decodeByteArray(itemData.imageData, 0, itemData.imageData!!.size).asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )
            }
        }
    }
}