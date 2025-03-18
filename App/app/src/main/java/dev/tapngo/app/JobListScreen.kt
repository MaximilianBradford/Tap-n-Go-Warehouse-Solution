package dev.tapngo.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils
import dev.tapngo.app.utils.inventreeutils.components.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun JobListScreen(navController: NavController) {
    var jobs by remember { mutableStateOf<List<Job>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val fetchedJobs = InvenTreeUtils.getUserJobs()
            withContext(Dispatchers.Main) {
                jobs = fetchedJobs
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        jobs?.let {
            JobList(jobs = it)
        }
    }
}