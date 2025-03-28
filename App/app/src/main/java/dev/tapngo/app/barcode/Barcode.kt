package dev.tapngo.app.barcode

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.compose.TapNGoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.tapngo.app.ui.ErrorScreen
import dev.tapngo.app.ui.LoadingScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Barcode(
    navController: NavController
) {
    // State to hold the scanned barcode value, saved across recompositions
    var barcode by rememberSaveable { mutableStateOf<String?>("No Code Scanned") }

    // State to manage the camera permission
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // State to track whether to show the rationale dialog for the permission
    var oncancel by remember(permissionState.status.shouldShowRationale) {
        mutableStateOf(permissionState.status.shouldShowRationale)
    }

    var barcodeCheck by remember { mutableStateOf(false) }

    // If no barcode has been scanned, show the QR/barcode scanner
    if (barcode == null && !barcodeCheck) {
        Log.d("Barcode", "Data sent $barcode")
        ScanCode(onQrCodeDetected = { detectedBarcode ->
            Log.d("Barcode", "ScanCode hit")
            if (detectedBarcode != "null" &&
                detectedBarcode.isNotBlank() &&
                detectedBarcode.matches(Regex("^\\d+$"))
            ) {
                barcode = detectedBarcode
                navController.navigate("barcode/${detectedBarcode}")
            } else {
                barcode = null
                barcodeCheck = true
            }
        })
    } else {
        // Fallback screen with error or permission handling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Show rationale dialog if permission is denied and rationale can be shown
            if (oncancel) {
                ShowRationaleDialog(
                    onDismiss = { oncancel = false },
                    onConfirm = { permissionState.launchPermissionRequest() },
                    body = permissionState.permission
                )
            }

            // Determine the text to show based on the permission state
            val textToShow = when {
                permissionState.status.shouldShowRationale ->
                    "Camera permission is important for this app. Please grant the permission."
                !permissionState.status.isGranted ->
                    "Camera permission required. Please grant the permission"
                barcodeCheck ->
                    "Invalid Barcode Scanned! Please try again."
                else ->
                    ""
            }

            Text(textToShow)

            // Show appropriate button based on permission state
            if (permissionState.status.isGranted) {
                Button(onClick = { barcode = null }) {
                    Icon(Icons.Filled.Camera, contentDescription = "Scan")
                    Spacer(Modifier.width(8.dp))
                    Text("Scan QR or Barcode")
                }
            } else {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request Camera Permission")
                }
            }
        }
    }
}