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
import dev.tapngo.app.ui.InventoryActivity
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils.Companion.getItemData
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import java.io.IOException


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

    // Entry point for the app.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        context = this
        Log.d("MainActivity", "onCreate called")

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
                            item = item,
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
    }

    /*
     * Callback for when NFC data is read
     *
     * I wish the NFCReader class could be self contained... It probably can, but I've got no time ~ Dan
     */
    override fun onNfcDataRead(data: String) {
        val regex = Regex("^\\d+:\\d+$")
        Log.d("MainActivity", "NFC data reads: $data")
        if (regex.matches(data)) {
            val split = data.split(":")
            item = getItemData(split[0].toInt(), split[1].toInt())
            showDialog.value = true
        }
    }
}

// Global variable to store the currently scanned item.
var item: ItemData? = null

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
                    Text(
                        "Waiting for NFC...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "Whoops, something is wrong with the NFC scanner. Please exit the app and try again with the scanner!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
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
            if (!isLoginScreen) {
                Log.d("App Bar", "Main App Bar Used")
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            IconButton(onClick = {
                                mainScreenState =
                                    MainScreenState.NFCScan; navController.popBackStack(
                                route = "main",
                                inclusive = false
                            )
                                Log.d("NavBar", "NFC Called")
                            }) {
                                Icon(Icons.Filled.Nfc, contentDescription = "NFC menu")
                            }
                            IconButton(onClick = {
                                mainScreenState =
                                    MainScreenState.ItemList; navController.popBackStack(
                                route = "main",
                                inclusive = false
                            )
                                Log.d("NavBar", "ItemList Called")
                            }) {
                                Icon(
                                    Icons.Filled.Notes,
                                    contentDescription = "Localized description",
                                )
                            }
                            IconButton(onClick = {
                                mainScreenState =
                                    MainScreenState.Barcode; navController.popBackStack(
                                route = "main",
                                inclusive = false
                            )
                                Log.d("NavBar", "Barcode Called")
                            }) {
                                Icon(
                                    Icons.Filled.Camera,
                                    contentDescription = "Barcode Reader",
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
                //Log.d("CheckoutDebug", "Before checkout: item = $item")
                CheckoutScreen(itemData = item!!, navController = navController)
            }
            composable("barcode/{barcode_id}",
                arguments = listOf(
                    navArgument("barcode_id") { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val barcode_id = navBackStackEntry.arguments?.getString("barcode_id")
                barcode_id?.let { barcode_id ->
                    try {
                        item =
                            if (barcode_id != null && barcode_id != "null" && barcode_id.isNotBlank() && barcode_id.matches(
                                    Regex("\\d+")
                                )
                            ) {
                                getItemData(barcode_id.toInt(), null)
                            } else {
                                null // or some default value
                            }
                    } catch (e: IOException) {
                        Log.e("Barcode", "Barcode scanner failed to retrieve item ${e.message}")
                    }
                    var showpop by remember { mutableStateOf(true) }
                    if (item != null) {
                        ItemPopup(
                            showDialog = showpop,
                            onDismiss = { showpop = false },
                            item = item,
                            navController = navController
                        )
                        //CheckoutScreen(itemData = item!!, navController = navController)
                    } else {
                        Text("Barcode Scan Failed, please try again")
                    }
                }
            }
        }
    }
}


// Cybersecurity is my passion! ~ Dan
var authToken: String? = null


// Constants for my testing servers ~ Dan
const val server = "10.0.2.2:8000" // Localhost
//const val server = "10.0.0.116:8080" // Desktop
//const val server = "###.###.###.###:8080" // Garage servers. (not posting the IP here)