package dev.tapngo.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dev.tapngo.app.R
import dev.tapngo.app.model.InventoryItem
import dev.tapngo.app.ui.theme.NfcScanActivity

class AddInventoryActivity : AppCompatActivity() {

    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var scanNfcButton: Button
    private lateinit var saveButton: Button
    private var scannedNfcTag: String? = null  // Stores the scanned NFC tag ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_inventory)

        itemNameEditText = findViewById(R.id.itemNameEditText)
        itemQuantityEditText = findViewById(R.id.itemQuantityEditText)
        scanNfcButton = findViewById(R.id.scanNfcButton)
        saveButton = findViewById(R.id.saveButton)

        scanNfcButton.setOnClickListener {
            // Launch NFC scanning activity
            val intent = Intent(this, NfcScanActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        saveButton.setOnClickListener {
            val name = itemNameEditText.text.toString()
            val quantity = itemQuantityEditText.text.toString().toIntOrNull()

            if (name.isNotEmpty() && quantity != null && scannedNfcTag != null) {
                val newItem = InventoryItem(
                    id = (Math.random() * 10000).toInt(), // Random ID
                    name = name,
                    quantity = quantity,
                    imageResId = R.drawable.ic_placeholder // Default placeholder image
                )
                // Save item (to database or API)
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
                finish() // Close activity
            } else {
                Toast.makeText(this, "Fill all fields and scan an NFC tag!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            scannedNfcTag = data?.getStringExtra("nfc_tag_id")
            Toast.makeText(this, "NFC Tag: $scannedNfcTag", Toast.LENGTH_SHORT).show()
        }
    }

}