package dev.tapngo.app.utils


import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.tapngo.app.R
import org.json.JSONObject

class NfcActivity : AppCompatActivity() {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var textView: TextView
    private lateinit var scanButton: Button
    private var isScanning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)

        textView = findViewById(R.id.nfcStatus)
        scanButton = findViewById(R.id.scanNfcButton)

        // Initialize NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            showErrorDialog("NFC Not Supported", "Your device does not support NFC.")
            scanButton.isEnabled = false
            return
        }

        scanButton.setOnClickListener {
            if (!nfcAdapter.isEnabled) {
                showErrorDialog("NFC Disabled", "Please enable NFC in settings.")
            } else {
                isScanning = !isScanning
                textView.text = if (isScanning) "Waiting for NFC Tag..." else "Tap an NFC tag to scan."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled) {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))

            try {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, arrayOf(arrayOf(NfcA::class.java.name)))
            } catch (e: Exception) {
                Log.e("NFC", "Error enabling NFC: ${e.message}")
                showErrorDialog("Error", "Failed to enable NFC scanning.")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter.isEnabled) {
            try {
                nfcAdapter.disableForegroundDispatch(this)
            } catch (e: Exception) {
                Log.e("NFC", "Error disabling NFC: ${e.message}")
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagId = it.id.joinToString(":") { byte -> "%02X".format(byte) }
                val jsonData = formatNfcData(tagId)
                showSuccessDialog(jsonData)
            } ?: run {
                showErrorDialog("Error", "Failed to read NFC tag.")
            }
        }
    }

    private fun formatNfcData(tagId: String): String {
        val jsonObject = JSONObject().apply {
            put("tag_id", tagId)
            put("timestamp", System.currentTimeMillis())
        }
        return jsonObject.toString(4) // Pretty print JSON
    }

    private fun showSuccessDialog(data: String) {
        AlertDialog.Builder(this)
            .setTitle("NFC Tag Scanned")
            .setMessage("Tag Data:\n$data")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showErrorDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
