package dev.tapngo.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tapngo.app.utils.inventreeutils.components.Location

@Composable
fun LocationTable(locations: List<Location>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Locations",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            locations.forEach { location ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = location.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = location.getQuantity().toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

        }
    }
}