package dev.tapngo.app

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.tapngo.app.utils.inventreeutils.components.Job

@Composable
fun JobItem(job: Job) {
    val statusText = when (job.status) {
        1 -> "Pending"
        2 -> "In Progress"
        3 -> "Complete"
        else -> "Unknown"
    }

    val statusColor = when (job.status) {
        1 -> Color.Red
        2 -> Color.Yellow
        3 -> Color.Green
        else -> Color.Gray
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = job.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = statusText,
                    color = statusColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = job.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${job.address.city}, ${job.address.state}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${job.address.zip}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { expanded = !expanded }) {
                Text("Show Items")
            }

            if(job.items.isNotEmpty() && expanded) {
                JobItemTable(job = job)
            }
        }
    }
}

@Composable
fun JobItemTable(job: Job) {
    job.items.forEach { item ->
        Row {
            Text(text = item.part.description!!)
            Text(text = item.quantity.toString())
        }
    }
}

@Composable
fun JobList(jobs: List<Job>) {
    LazyColumn {
        items(jobs) { job ->
            JobItem(job = job)
        }
    }
}