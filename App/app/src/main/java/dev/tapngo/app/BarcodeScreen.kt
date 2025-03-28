package dev.tapngo.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import dev.tapngo.app.ui.ErrorScreen
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils.Companion.getPartFromStockNo
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BarcodeScreen(barcodeId: String, navController: NavController) {
    if (!barcodeId.isDigitsOnly()) {
        ErrorScreen(errorMessage = "Barcode Scan Failed, please try again")
        return
    }

    var item: ItemData? by remember { mutableStateOf(null) }
    var showpop by remember { mutableStateOf(true) }

    LaunchedEffect(barcodeId) {
        item = withContext(Dispatchers.IO) {
            getPartFromStockNo(barcodeId.toInt())
        }
    }

    if (item != null) {
        ItemPopup(
            showDialog = showpop,
            onDismiss = { showpop = false },
            item = item,
            navController = navController
        )
    } else {
        ErrorScreen(errorMessage = "Barcode Scan Failed, please try again")
    }
}