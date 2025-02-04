package dev.tapngo.app.utils

import android.app.PendingIntent
import android.content.Intent
import android.nfc.FormatException
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.UnsupportedEncodingException

class NFCActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)

        textView = findViewById(R.id.nfcStatus)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                try {
                    // Handle NFC tag reading
                    textView.text = "NFC Tag Detected: ${tag.id.contentToString()}"
                    readNfcTag(it)
                } catch (e: NullPointerException) {
                    textView.text = "Error: Tag is null"
                    e.printStackTrace()
                } catch (e: UnsupportedEncodingException) {
                    textView.text = "Error: Unsupported Encoding"
                    e.printStackTrace()
                } catch (e: IOException) {
                    textView.text = "Error: I/O Error while reading the NFC tag"
                    e.printStackTrace()
                } catch (e: FormatException) {
                    textView.text = "Error: Invalid NFC Tag Format"
                    e.printStackTrace()
                } catch (e: Exception) {
                    textView.text = "Unexpected Error occurred during NFC scanning"
                    e.printStackTrace()
                }
            }
        }
    }

    private fun readNfcTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            val ndefMessage = ndef.cachedNdefMessage
            val records = ndefMessage.records
            if (records.isNotEmpty()) {
                val payload = records[0].payload
                val text = String(payload)
                textView.text = "NFC Data: $text"
            } else {
                textView.text = "NFC Tag is empty"
            }
        } catch (e: NullPointerException) {
            throw NullPointerException("NFC tag or NDEF is null")
        } catch (e: IOException) {
            throw IOException("I/O Error while reading the NFC tag")
        } catch (e: Exception) {
            throw Exception("Unknown error occurred while reading NFC tag")
        }
    }
}
