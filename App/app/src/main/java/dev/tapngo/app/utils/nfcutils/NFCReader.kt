package dev.tapngo.app

//I used Claude 3.5 to refine this code a bit and hopefully stop giving me the bloody NFC Forground Dispatch issue.
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.IOException
import java.nio.charset.Charset

class NFCReader(
    private val activity: ComponentActivity,  // Changed from Context to ComponentActivity
    private val nfcAdapter: NfcAdapter?,
    private val callback: NFCReaderCallback
) {
    // Store PendingIntent as a class property to avoid recreating it
    private var pendingIntent: PendingIntent? = null
    var isScanning = false
    init {
        Log.d("NFCReader", "NFCReader initialized")
        if (nfcAdapter == null) {
            Log.e("NFCReader", "NFC Adapter is null")
            Toast.makeText(activity, "NFC is not enabled on this device.", Toast.LENGTH_LONG).show()
        } else {
            // Create PendingIntent once during initialization
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    activity,
                    0,
                    Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    activity,
                    0,
                    Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        }
    }

    fun startNfcScanner() {
        Log.d("NFCReader", "startNfcScanner called")
        enableNfcForegroundDispatch()
    }

    fun enableNfcForegroundDispatch() {
        Log.d("NFCReader", "enableNfcForegroundDispatch called")
        try {
            nfcAdapter?.let { adapter ->
                if (adapter.isEnabled) {
                    val nfcIntentFilter = arrayOf(
                        IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                        IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                        IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                    )

                    adapter.enableForegroundDispatch(
                        activity,  // No casting needed anymore
                        pendingIntent,
                        nfcIntentFilter,
                        null
                    )
                    isScanning = true
                    Log.d("NFCReader", "NFC foreground dispatch enabled successfully")
                } else {
                    Log.w("NFCReader", "NFC adapter is not enabled")
                }
            }
        } catch (e: IllegalStateException) {
            Log.e("NFCReader", "Failed to enable foreground dispatch: ${e.message}")
            isScanning = false
        }
    }

    fun disableNfcForegroundDispatch() {
        Log.d("NFCReader", "disableNfcForegroundDispatch called")
        try {
            if (isScanning) {
                nfcAdapter?.disableForegroundDispatch(activity)
                isScanning = false
                Log.d("NFCReader", "NFC foreground dispatch disabled successfully")
            }
        } catch (e: Exception) {
            Log.e("NFCReader", "Error disabling foreground dispatch", e)
        }
    }

    fun handleNfcIntent(intent: Intent) {
        Log.d("NFCReader", "handleNfcIntent called with action: ${intent.action}")

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
        ) {

            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            Log.d("NFCReader", "NFC tag detected: $tag")

            tag?.let {
                val ndef = Ndef.get(it)
                ndef?.let { ndefTag ->
                    try {
                        ndefTag.connect()
                        val ndefMessage = ndefTag.cachedNdefMessage

                        for (record in ndefMessage.records) {
                            if (record.tnf == NdefRecord.TNF_WELL_KNOWN &&
                                record.type.contentEquals(NdefRecord.RTD_TEXT)
                            ) {

                                val payload = record.payload
                                val textEncoding = if ((payload[0].toInt() and 0x80) == 0)
                                    Charset.forName("UTF-8") else Charset.forName("UTF-16")
                                val languageCodeLength = payload[0].toInt() and 0x3F

                                val text = String(
                                    payload,
                                    languageCodeLength + 1,
                                    payload.size - languageCodeLength - 1,
                                    textEncoding
                                )

                                callback.onNfcDataRead(text)
                                Log.d("NFCReader", "NDEF text record: $text")
                            }
                        }
                        ndefTag.close()
                    } catch (e: IOException) {
                        Log.e("NFCReader", "Error reading NDEF message", e)
                        Toast.makeText(activity, "Error reading NDEF message", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    interface NFCReaderCallback {
        fun onNfcDataRead(data: String)
    }
}