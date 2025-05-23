package dev.tapngo.app

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.inventreeutils.components.Job
import dev.tapngo.app.utils.inventreeutils.components.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TransferComponent(itemData: ItemData, jobs: List<Job>, navController: NavController) {
    var selectedFromLocation by remember { mutableStateOf(itemData.selectedLocation) }
    var selectedToJob by remember { mutableStateOf<Job?>(null) }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(TextFieldValue("")) }
    val coroutineScope = rememberCoroutineScope()

    Log.d("TransferComponent", "Jobs: $jobs")


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (itemData.selectedLocation == null) {
                Box(modifier = Modifier
                    .weight(1f)
                    .height(48.dp)) {
                    Button(onClick = { expandedFrom = true }) {
                        Text(text = selectedFromLocation?.name ?: "Select Location")
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
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier
                .weight(1f)
                .height(48.dp)) {
                Button(onClick = { expandedTo = true }) {
                    Text(text = selectedToJob?.name ?: "Select Job")
                }
                DropdownMenu(
                    expanded = expandedTo,
                    onDismissRequest = { expandedTo = false }
                ) {
                    jobs.forEach { job ->
                        DropdownMenuItem(text = { Text(job.name) }, onClick = {
                            selectedToJob = job
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

        selectedToJob?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Move To Job: ${it.name}", color = MaterialTheme.colorScheme.primary)
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
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth(),
            onClick = {
                coroutineScope.launch {
                    selectedFromLocation?.let { from ->
                        selectedToJob?.let { to ->
                            val qty = quantity.text.toIntOrNull() ?: 0
                            if (qty > 0) {
                                InvenTreeUtils.transferItemJob(itemData, from, to, qty, navController)
                            }
                        }
                    }
                }
            },
            enabled = selectedFromLocation != null && selectedToJob != null && (quantity.text.toIntOrNull()
                ?: 0) > 0
        ) {
            Text("Confirm")
        }
    }
}