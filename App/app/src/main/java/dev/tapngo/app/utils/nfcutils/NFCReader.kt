package dev.tapngo.app

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.nio.charset.Charset


/*
 * Class for the NFC reader.
 * Extremely gross.
 */
class NFCReader(private val context: Context, private val nfcAdapter: NfcAdapter?, private val callback: NFCReaderCallback) {

    // Store this for a stupid reason
    var isScanning = false

    init {
        Log.d("NFCReader", "NFCReader initialized")
        if(nfcAdapter == null) {
            Log.e("NFCReader", "NFC Adapter is null")
            Toast.makeText(context, "NFC is not enabled on this device.", Toast.LENGTH_LONG).show()
        } else {
            startNfcScanner()
        }
    }

    // Start the scanner.
    fun startNfcScanner() {
        Log.d("NFCReader", "startNfcScanner called")
        isScanning = true
        enableNfcForegroundDispatch()
    }

    // I lied about above. THIS actually starts the scanner.
    // Get bamboozled!
    fun enableNfcForegroundDispatch() {
        Log.d("NFCReader", "enableNfcForegroundDispatch called")
        // If the adapter is null, this gets skipped.
        nfcAdapter?.let { adapter ->
            // Make sure it's enabled, otherwise literally nothing will happen.
            if (adapter.isEnabled) {
                // List of filters.
                // Specifies the tags I'm looking for.
                val nfcIntentFilter = arrayOf(
                    IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                    IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
                    IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                )

                // Create pending intent.
                // Basically allows the code to be ran by the system when an above filter is met.
                // Depends on version because some goofball with an 80 year old phone might be using this. ~ Dan
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, context.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, context.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
                // Actually enable the reader!!!
                adapter.enableForegroundDispatch(
                    context as MainActivity, pendingIntent, nfcIntentFilter, null
                )
            }
        }
    }

    // Disable the reader when the app is paused.
    // Although it would be nice to have the reader constantly running so users can just tap their phone. It would probably cause issues. ~ Dan
    fun disableNfcForegroundDispatch() {
        Log.d("NFCReader", "disableNfcForegroundDispatch called")
        if (isScanning) {
            nfcAdapter?.disableForegroundDispatch(context as MainActivity)
        }
    }

    // The code to run when an NFC tag is scanned
    fun handleNfcIntent(intent: Intent) {
        Log.d("NFCReader", "handleNfcIntent called with action: ${intent.action}")

        // Theoretically I can remove this check since the filters are applied above.
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action || NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            // TODO - Not use deprecated method
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            Log.d("NFCReader", "NFC tag detected: $tag")
            // If the tag is null, this gets skipped.
            tag?.let {
                // Yippee! The tag is not null
                val ndef = Ndef.get(it)
                if (ndef != null) {
                    try {
                        // Start ripping data from the tag
                        ndef.connect()
                        val ndefMessage = ndef.cachedNdefMessage

                        // Get the records and loop thru them
                        val records = ndefMessage.records
                        for (record in records) {
                            // Ignore everything that isn't a text record.
                            // I use specifically text and not URL because I don't want to open a browser when scanning a tag.
                            // I also don't want to change every tag when I change domain.
                            if (record.tnf == NdefRecord.TNF_WELL_KNOWN && record.type.contentEquals(NdefRecord.RTD_TEXT)) {
                                // The payload is the actual data.. However it's a ByteArray.
                                val payload = record.payload

                                // The first byte is the language code length and encoding.

                                // The first bit of the first byte is the encoding.
                                // If the first bit is 0, it's UTF-8. If it's 1, it's UTF-16.
                                // The value is found by ANDing the first byte with 0x80. Simply, extract the first bit.
                                // Found the language code here: https://www.netes.com.tr/netes/dosyalar/dosya/B6159F60458582512B16EF1263ADE707.pdf
                                // Found inspiration here: https://github.com/underwindfall/NFCAndroid/blob/0b15d14e50cc7d339f623dd990aa358010eefab8/nfcemvread/src/main/java/com/qifan/nfcemvread/extensions/TextRecord.java#L42
                                val textEncoding = if ((payload[0].toInt() and 0x80) == 0) Charset.forName("UTF-8") else Charset.forName("UTF-16")

                                // Similar process as above, however this only uses the lower 6 bits.
                                // This is the length of the language code.
                                // This stumped me for a while. This is literally the length of the string that tells the system what language the text is in. For example, 000011 could be "eng".
                                // More information on language codes: https://www.netes.com.tr/netes/dosyalar/dosya/B6159F60458582512B16EF1263ADE707.pdf
                                // Also inspired by: https://github.com/underwindfall/NFCAndroid/blob/0b15d14e50cc7d339f623dd990aa358010eefab8/nfcemvread/src/main/java/com/qifan/nfcemvread/extensions/TextRecord.java#L50
                                val languageCodeLength = payload[0].toInt() and 0x3F

                                // according to the github repo above. I can get the language code by feeding in the payload and the length of the language code into a string??!
                                // TODO - Check out later NFC tech is cool.

                                // This line was taken from here: https://github.com/underwindfall/NFCAndroid/blob/0b15d14e50cc7d339f623dd990aa358010eefab8/nfcemvread/src/main/java/com/qifan/nfcemvread/extensions/TextRecord.java#L52
                                val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, textEncoding)

                                // FINALLY WE HAVE THE TEXT!!!!
                                callback.onNfcDataRead(text)
                                Log.d("NFCReader", "NFCD text record: $text")
                            }
                        }
                        ndef.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("NFCReader", "Error reading NDEF message", e)
                        Toast.makeText(context, "Error reading NDEF message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    interface NFCReaderCallback {
        fun onNfcDataRead(data: String)
    }
}