package dev.tapngo.app.utils.internetcheck

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Interval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var monitoringJob: Job? = null

    private fun checkForInternet(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true


                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    fun connectionMonitor(onConnectionChange: (Boolean) -> Unit, intervalMs: Long = 5000) {
        stopMonitoring()
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            var previousState = checkForInternet()
            withContext(Dispatchers.Main) {
                onConnectionChange(previousState)
            }
            while (isActive) {

                delay(intervalMs)
                val currentState = checkForInternet()
                if (currentState != previousState) {
                    previousState = currentState
                    withContext(Dispatchers.Main){
                        onConnectionChange(currentState)
                    }
                }
                //Log.d("Connection", "Checking connection $currentState")
            }

        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }
}