package dev.tapngo.app

//import dev.tapngo.app.ui.theme.TapNGoTheme
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.compose.TapNGoTheme
import com.example.compose.surfaceDark
import com.example.compose.surfaceLight
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.setBothThemeColor


// Checkout screen component
//@Composable
//fun CheckoutScreen(itemData: ItemData, navController: NavController) {
//    val transfer = remember { mutableStateOf(false) }
//    Log.d("Check-out", "${itemData.sku}")
//    TapNGoTheme {
//        // I don't even center this one
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = "Checkout",
//                style = MaterialTheme.typography.titleLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            // It's safe to assume that the itemData is not null
//            // Even if it is, theoretically the following values will just simply not appear.
//            Text(text = "SKU: ${itemData.sku}", color = MaterialTheme.colorScheme.primary)
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = "Description: ${itemData.description}",
//                color = MaterialTheme.colorScheme.primary
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            if (itemData.imageData != null) {
//                Image(
//                    bitmap = BitmapFactory.decodeByteArray(
//                        itemData.imageData,
//                        0,
//                        itemData.imageData!!.size
//                    ).asImageBitmap(),
//                    contentDescription = null,
//                    modifier = Modifier.size(128.dp)
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))
//            itemData.locations?.let { LocationTable(it) }
//            Spacer(modifier = Modifier.height(16.dp))
//            if (transfer.value) {
//                if(itemData.selectedLocation != null) {
//                    Text("Transfer directly from ${itemData.selectedLocation?.name}")
//                } else {
//                    TransferComponent(itemData, navController)
//                }
//            } else {
//                Button(onClick = { transfer.value = true }) {
//                    Text("Transfer")
//                }
//            }
//        }
//    }
//}

@Composable
fun CheckoutScreen(itemData: ItemData, navController: NavController) {
    val transfer = remember { mutableStateOf(false) }
    Log.d("Check-out", "${itemData.sku}")

    TapNGoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = setBothThemeColor(
                        lightColor = surfaceLight,
                        darkColor = surfaceDark
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    itemData.imageData?.let {
                        Image(
                            bitmap = BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap(),
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Default Image",
                        modifier = Modifier.size(150.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "SKU: ${itemData.sku ?: "No SKU"}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = itemData.description ?: "No Description",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            itemData.locations?.let {
                LocationTable(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (transfer.value) {
                if (itemData.selectedLocation != null) {
                    Text(
                        text = "Transferring from: ${itemData.selectedLocation?.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    TransferComponent(itemData, navController)
                }
            } else {
                Button(
                    onClick = { transfer.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        "Transfer", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CheckoutScreenPreview() {
    val dummyItem = ItemData(id = 1, loc = null).apply {
        sku = "12345XYZ"
        description = "Sample Item for Checkout"
    }

    val mockNavController = rememberNavController() // Mock NavController for preview

    CheckoutScreen(itemData = dummyItem, navController = mockNavController)
}