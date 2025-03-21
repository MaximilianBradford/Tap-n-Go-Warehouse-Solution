package dev.tapngo.app.barcode

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

//Referenced from https://github.com/DUMA042/BarsandQ/tree/master
@Composable
fun ShowRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Permission",
    body: String = "Permission needed"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = body)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}