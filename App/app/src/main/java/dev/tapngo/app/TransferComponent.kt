package dev.tapngo.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.inventreeutils.components.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TransferComponent(itemData: ItemData, navController: NavController) {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var selectedFromLocation by remember { mutableStateOf<Location?>(null) }
    var selectedToLocation by remember { mutableStateOf<Location?>(null) }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun getData() {
        locations = withContext(Dispatchers.IO) {
            InvenTreeUtils.getLocationList(null, null)
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            getData()
        }
    }

    Column {
        Row {
            Box(modifier = Modifier.weight(1f)) {
                Button(onClick = { expandedFrom = true }) {
                    Text(text = selectedFromLocation?.name ?: "Transfer From")
                }
                DropdownMenu(
                    expanded = expandedFrom,
                    onDismissRequest = { expandedFrom = false }
                ) {
                    itemData.locations?.forEach { location ->
                        DropdownMenuItem(text = { Text(location.name) }, onClick = {
                            selectedFromLocation = location
                            expandedFrom = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                Button(onClick = { expandedTo = true }) {
                    Text(text = selectedToLocation?.name ?: "Transfer To")
                }
                DropdownMenu(
                    expanded = expandedTo,
                    onDismissRequest = { expandedTo = false }
                ) {
                    locations.forEach { location ->
                        DropdownMenuItem(text = { Text(location.name) }, onClick = {
                            selectedToLocation = location
                            expandedTo = false
                        })
                    }
                }
            }
        }

        selectedFromLocation?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Pull From Location: ${it.name}", color = MaterialTheme.colorScheme.primary)
        }

        selectedToLocation?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Move To Location: ${it.name}", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    selectedFromLocation?.let { from ->
                        selectedToLocation?.let { to ->
                            val qty = quantity.text.toIntOrNull() ?: 0
                            if (qty > 0) {
                                InvenTreeUtils.transferItemWH(from, to, qty, navController)
                            }
                        }
                    }
                }
            },
            enabled = selectedFromLocation != null && selectedToLocation != null && (quantity.text.toIntOrNull()
                ?: 0) > 0
        ) {
            Text("Confirm")
        }
    }
}