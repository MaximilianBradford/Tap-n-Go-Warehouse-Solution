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
import dev.tapngo.app.utils.itemUtils.getItem
import dev.tapngo.app.utils.itemUtils.updateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BarcodeScreen(barcodeId: String, navController: NavController) {
    if (!barcodeId.isDigitsOnly()) {
        ErrorScreen(errorMessage = "Barcode Scan Failed, please try again")
        return
    }
    
    var showpop by remember { mutableStateOf(true) }
    var founditem: ItemData?
    LaunchedEffect(barcodeId) {
        founditem = withContext(Dispatchers.IO) {
            getPartFromStockNo(barcodeId.toInt())
        }
        founditem?.let { updateItem(it) }
    }

    if (getItem().id == -1) {
        ItemPopup(
            showDialog = showpop,
            onDismiss = { showpop = false },
            item = getItem(),
            navController = navController
        )
    } else {
        ErrorScreen(errorMessage = "Barcode Scan Failed, please try again")
    }
}