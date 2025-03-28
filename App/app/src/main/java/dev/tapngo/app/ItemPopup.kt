package dev.tapngo.app

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.tapngo.app.utils.inventreeutils.components.ItemData

// The little popup right when you scan an item
// This is CONSTANTLY on your screen... My apologies ~ Dan
@Composable
fun ItemPopup(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    item: ItemData?,
    navController: NavController
) {
    // Hide the dialog if showDialog is false
    if (showDialog && item != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Item Details")
            },
            text = {
                // Item information
                Column {
                    Text(text = "SKU: ${item.sku}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Description: ${item.description}")
                    if(item.selectedLocation != null){
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Location: ${item.selectedLocation!!.name}")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (item.imageData != null) {
                        Image(
                            // Shoutout to android docs for this bit!! https://developer.android.com/reference/android/graphics/BitmapFactory
                            bitmap = BitmapFactory.decodeByteArray(
                                item.imageData,
                                0,
                                item.imageData!!.size
                            ).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(128.dp)
                        )
                    }
                }
            },
            // If the displayed item is correct, the user will click on this.
            // Redirects to the item's checkout page
            confirmButton = {
                Button(onClick = {
                    onDismiss()
                    // Funny thing about this is that the parameters aren't even used.
                    Log.d("nfc press", "${item.sku}")
                    navController.navigate("checkout/${item.sku}")
                }) {
                    Text("Continue")
                }
            },
            // Today I learned about the dismiss button!!! ~ Dan
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

