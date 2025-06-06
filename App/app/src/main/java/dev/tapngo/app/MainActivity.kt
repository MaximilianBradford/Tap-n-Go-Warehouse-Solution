package dev.tapngo.app

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.TapNGoTheme
import dev.tapngo.app.barcode.Barcode
import dev.tapngo.app.ui.ErrorScreen
import dev.tapngo.app.ui.InventoryActivity
import dev.tapngo.app.ui.LoadingScreen
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils.Companion.getItemData
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils.Companion.getPartFromStockNo
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import dev.tapngo.app.utils.itemUtils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import dev.tapngo.app.utils.internetcheck.*
import kotlinx.coroutines.delay


/*
 * Main activity for the app
 * All processes stem off of this.
 * This also contains the callback for the NFC reader.. Which I really wish I could move to the NFCReader class
 */

class MainActivity : ComponentActivity(), NFCReader.NFCReaderCallback {
    // NFC adapter
    private var nfcAdapter: NfcAdapter? = null

    // Dialog state
    // I am so sorry... ~ Dan
    private val showDialog = mutableStateOf(false)

    // controller
    private lateinit var navController: NavHostController

    //Monitor for network connection
    private lateinit var networkMonitor: NetworkMonitor

    // Entry point for the app.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity", "onCreate called")
        networkMonitor = NetworkMonitor(applicationContext)
        // Get the NFC service and cast it to NfcManager to get the default adapter
        // I feel like directly casting the service to NfcManager is a terrible idea ~ Dan
        nfcAdapter = (getSystemService(NFC_SERVICE) as NfcManager).defaultAdapter

        // I don't need a null check here since NFCReader handles it ~ Dan

        // Initialize the NFC reader... I can probably remove the "callback" parameter and just use context. ~ Dan
        nfcReader = NFCReader(this, nfcAdapter, this)

        // Building the UI
        setContent {
            TapNGoTheme() {
                // Creating a basic nav controller.
                // TODO - Store previous and current locations in a stack to allow for back navigation ~Dan
                navController = rememberNavController()

                // This scaffold holds the item popup.
                // This is absolutely terrible. It is the equivalent of making a fixed div with a full width/height and setting display to none. ~Dan
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // The item popup which is by default, hidden.
                        ItemPopup(
                            showDialog = showDialog.value,
                            onDismiss = { showDialog.value = false },
                            item = getItem(),
                            navController = navController
                        )
                    }
                }
                // Underneath the stuff that would get me cursed out by anyone who knows what they're doing, we have the navigation. ~Dan
                AppNavHost(navController)
            }
        }


    }

    // This is the method that will be called when the "Start" button is clicked
    fun onStartButtonClicked(view: View) {
        // Handle button click here (for example, show a toast message)
        Toast.makeText(this, "Start Button Clicked", Toast.LENGTH_SHORT).show()

        // Navigating to InventoryActivity
        val intent = Intent(this, InventoryActivity::class.java)
        startActivity(intent)
    }

    /*
     * Enable NFC foreground dispatch when the activity is resumed
     * This is called when the activity is opened again.
     */
    override fun onResume() {
        super.onResume()
        networkMonitor.connectionMonitor(

            onConnectionChange = { isConnected ->
                if (isConnected) {
                    connectionStatus = true
                    Toast.makeText(this, "WiFi connected", Toast.LENGTH_SHORT).show()

                } else {
                    connectionStatus = false
                    Toast.makeText(this, "WiFi disconnected", Toast.LENGTH_SHORT).show()
                }
            },
            intervalMs = 3000

        )
        Log.d("MainActivity", "onResume called")
        window.decorView.post { //Getting persistent errors with nfcreader enabling before the system is ready. Trying this fix.
            nfcReader?.enableNfcForegroundDispatch()
        }
    }

    /*
     * Disable NFC foreground dispatch when the activity is paused
     * This is called when someone switches to another app or the home screen
     *
     * For some reason this didn't work until I made the NFC reader class ~ Dan
     */
    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        nfcReader?.disableNfcForegroundDispatch()
        networkMonitor.stopMonitoring()
    }

    /*
     * Handle NFC intent when the activity is opened
     *
     * Other intents can probably be placed in here as well. Idk what yet ~ Dan
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        nfcReader?.handleNfcIntent(intent)
        networkMonitor.stopMonitoring()
    }

    /*
     * Callback for when NFC data is read
     *
     * I wish the NFCReader class could be self contained... It probably can, but I've got no time ~ Dan
     */
    override fun onNfcDataRead(data: String) {
        Log.d("MainActivity", "NFC data reads: $data")
        if (data.isDigitsOnly()) {
            var item: ItemData? = null
            CoroutineScope(Dispatchers.IO).launch {
                item = getPartFromStockNo(data.toInt())
                item?.let { updateItem(it) }
            }
            while (getItem().id == -1) {
                Thread.sleep(100)
            }
            showDialog.value = true
        }
    }



}
var connectionStatus: Boolean = false


// reader
var nfcReader: NFCReader? = null



//var context: Context? = null

/*
 *
 */
// Class made with assistance from Claude AI to help with bottom bar button function.
sealed class MainScreenState {
    object NFCScan : MainScreenState() {}    // For NFC scanning screen
    object ItemList : MainScreenState()    // For showing items
    object Barcode : MainScreenState()
    object Job : MainScreenState()
}

/*
* Main screen composable
*
* Dynamic menu that can change the current function depending on user need.
*/
@Composable
fun MainScreen(
    currentScreen: MainScreenState = MainScreenState.NFCScan,
    navController: NavController,
) {
    Column {
        when (currentScreen) {
            is MainScreenState.NFCScan -> {
                if (nfcReader != null && nfcReader!!.isScanning) {
                    LoadingScreen(
                        loadingMessage = "Waiting for NFC Tag...", navController
                    )
                }
                else {
                    ErrorScreen(errorMessage = "Whoops, something is wrong with the NFC scanner. Please exit the app and try again with the scanner!")
                }
            }

            is MainScreenState.ItemList -> {
                ItemList(
                    navController = navController,
                )

            }
            is MainScreenState.Barcode -> {
                Barcode(navController = navController)
            }

            is MainScreenState.Job -> {
                JobListScreen(navController = navController)
            }

        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    var mainScreenState by remember { mutableStateOf<MainScreenState>(MainScreenState.ItemList) }

    // variable that is kept constant across recomps
    var isLoginScreen by remember { mutableStateOf(true) }

    //Sets up a side effect that runs when navController changes, then updates isLoginScreen to true or false
    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            isLoginScreen = destination.route == "login"
        }
    }

    Scaffold(
        bottomBar = {
            if (!isLoginScreen && connectionStatus) {
                Log.d("App Bar", "Main App Bar Used")
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            IconButton(onClick = {
                                mainScreenState = MainScreenState.NFCScan; navController.popBackStack(
                                route = "main",
                                inclusive = false
                            )
                                Log.d("NavBar", "Scanning Called")
                            }) {
                                Icon(
                                    Icons.Filled.ImageSearch,
                                    contentDescription = "Scanning Mode"
                                )
                            }
                            IconButton(onClick = {
                                mainScreenState =
                                    MainScreenState.ItemList; navController.popBackStack(
                                route = "main",
                                inclusive = false
                            )
                                //Log.d("NavBar", "ItemList Called")
                            }) {
                                Icon(
                                    Icons.Filled.Notes,
                                    contentDescription = "Localized description",
                                )
                            }
                            IconButton(onClick = {
                                mainScreenState =
                                    MainScreenState.Job; navController.popBackStack(
                                route = "jobs",
                                inclusive = false
                            )
                                Log.d("NavBar", "Job List Called")
                            }) {
                                Icon(
                                    Icons.Filled.Work,
                                    contentDescription = "Job List",
                                )
                            }
                        }
                    },
                )
            } else {
                Log.d("App Bar", "Login App Bar Used")
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("main") {
                MainScreen(
                    mainScreenState, navController,
                )
            }
            composable("jobs") { JobListScreen(navController = navController) }
            composable(
                "checkout/{sku}",
                arguments = listOf(
                    navArgument("sku") { type = NavType.StringType },
                    )
            ) {
                Log.d("CheckoutDebug", "Before checkout: item = ${getItem()}")
                var error by remember { mutableStateOf<String?>(null) }
                var item by remember { mutableStateOf<ItemData?>(null) }
                LaunchedEffect(Unit) {
                    try {
                        while (getItem().id == -1){
                            delay(100)
                        }
                        item = getItem()
                    } catch (e:Exception) {
                        error = e.message ?: "Unknown Error"
                    }
                }
                when {
                    error != null -> MainScreen(
                        mainScreenState, navController,
                    )
                    item != null -> CheckoutScreen(itemData = item!!, navController = navController)
                }
            }
            composable("barcode/{barcode_id}",
                arguments = listOf(
                    navArgument("barcode_id") { type = NavType.StringType }
                )
            ) { navBackStackEntry ->

                val barcodeId = navBackStackEntry.arguments?.getString("barcode_id") ?: return@composable
                BarcodeScreen(barcodeId, navController)
            }
            composable("nfc")
            {
                if (nfcReader != null && nfcReader!!.isScanning) {
                    LoadingScreen(loadingMessage = "Waiting for NFC Tag...", navController)
                } else {
                    ErrorScreen(errorMessage = "Whoops, something is wrong with the NFC scanner. Please exit the app and try again with the scanner!")
                }
            }
            composable("barcode")
            {
                Barcode(navController)
            }

        }
    }
}




// Cybersecurity is my passion! ~ Dan
var authToken: String? = null


// Constants for my testing servers ~ Dan
//const val server = "10.0.2.2:8000" // Localhost
const val server = "10.0.0.116" // Desktop

//const val server = "###.###.###.###:8080" // Garage servers. (not posting the IP here)