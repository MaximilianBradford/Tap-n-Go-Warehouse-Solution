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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.tapngo.app.ui.theme.TapNGoTheme
import dev.tapngo.app.utils.inventreeutils.InvenTreeUtils
import dev.tapngo.app.utils.inventreeutils.components.ItemData
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import dev.tapngo.app.ui.InventoryActivity


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

    // reader
    private lateinit var nfcReader: NFCReader


    // Entry point for the app.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")

        // Get the NFC service and cast it to NfcManager to get the default adapter
        // I feel like directly casting the service to NfcManager is a terrible idea ~ Dan
        nfcAdapter = (getSystemService(NFC_SERVICE) as NfcManager).defaultAdapter

        // I don't need a null check here since NFCReader handles it ~ Dan

        // Initialize the NFC reader... I can probably remove the "callback" parameter and just use context. ~ Dan
        nfcReader = NFCReader(this, nfcAdapter, this)

        // Start the NFC scanner
        nfcReader.startNfcScanner()


        // Building the UI
        setContent {
            TapNGoTheme {
                // Creating a basic nav controller.
                // TODO - Store previous and current locations in a stack to allow for back navigation ~Dan
                navController = rememberNavController()

                // This scaffold holds the item popup.
                // This is absolutely terrible. It is the equivalent of making a fixed div with a full width/height and setting display to none. ~Dan
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
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
        nfcReader.enableNfcForegroundDispatch()
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
        nfcReader.disableNfcForegroundDispatch()
    }

    /*
     * Handle NFC intent when the activity is opened
     *
     * Other intents can probably be placed in here as well. Idk what yet ~ Dan
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with action: ${intent.action}")
        nfcReader.handleNfcIntent(intent)
    }

    /*
     * Callback for when NFC data is read
     *
     * I wish the NFCReader class could be self contained... It probably can, but I've got no time ~ Dan
     */
    override fun onNfcDataRead(data: String) {
        Log.d("MainActivity", "NFC data reads: $data")
        if(data.isDigitsOnly()){
            item = InvenTreeUtils.getItemData(data.toInt())
            showDialog.value = true
        }
    }
}

// Global variable to store the currently scanned item.
var item: ItemData? = null


/*
 * Composable function to handle navigation
 *
 * This is pretty terrible. ~Dan
 * TODO - Research how to do this properly
 */
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen() }
        composable(
            // Parameters aren't even used here... ~ Dan
            // The item gets passed in as an object.
            // Probably can remove itm but eh.. Can probably be used later for something.
            "checkout/{sku}",
            arguments = listOf(
                navArgument("sku") { type = NavType.StringType }
            )
        ) {
            CheckoutScreen(itemData = item!!)
        }
    }
}

/*
 * Main screen composable
 *
 * Literally just the default screen that says "Waiting for NFC..."
 */
@Composable
fun MainScreen() {
    Column {
        Text("Waiting for NFC...", style = MaterialTheme.typography.titleLarge, color = Color.White)
    }
}

// Cybersecurity is my passion! ~ Dan
var authToken: String? = null


// Constants for my testing servers ~ Dan
const val server = "10.0.2.2:8080" // Localhost
//const val server = "localhost:8000" // Localhost
//const val server = "192.168.4.22:8080" // Desktop
//const val server = "###.###.###.###:8080" // Garage servers. (not posting the IP here)