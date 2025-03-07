package dev.tapngo.app.ui.theme

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.tapngo.app.R

class NfcScanActivity : AppCompatActivity()  {

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var tagTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_scan)

        tagTextView = findViewById(R.id.tagTextView)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))

        try {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null)
        } catch (e: Exception) {
            Toast.makeText(this, "Error enabling NFC: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            nfcAdapter.disableForegroundDispatch(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Error disabling NFC: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent?.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                val tagId = it.id.joinToString(":") { byte -> "%02X".format(byte) }
                tagTextView.text = "NFC Tag Detected: $tagId"

                // Send the scanned tag back to the calling activity
                val resultIntent = Intent()
                resultIntent.putExtra("nfc_tag_id", tagId)
                setResult(RESULT_OK, resultIntent)
                finish()
            } ?: run {
                Toast.makeText(this, "Failed to read NFC tag.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}