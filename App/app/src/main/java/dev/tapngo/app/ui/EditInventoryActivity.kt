package dev.tapngo.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import dev.tapngo.app.R
import dev.tapngo.app.model.InventoryItem

class EditInventoryActivity : AppCompatActivity() {
    private lateinit var itemNameEditText: EditText
    private lateinit var itemQuantityEditText: EditText
    private lateinit var itemImageView: ImageView
    private lateinit var saveButton: Button
    private var itemId: Int = -1
    private var itemImageResId: Int = -1  // Store image ID to keep it unchanged

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_inventory)

        // Initialize UI components
        itemNameEditText = findViewById(R.id.editItemName)
        itemQuantityEditText = findViewById(R.id.editItemQuantity)
        itemImageView = findViewById(R.id.itemImageView)
        saveButton = findViewById(R.id.saveButton)

        // Retrieve item details from intent
        itemId = intent.getIntExtra("item_id", -1)
        val itemName = intent.getStringExtra("item_name") ?: ""
        val itemQuantity = intent.getIntExtra("item_quantity", 0)
        itemImageResId = intent.getIntExtra("item_image", R.drawable.ic_placeholder)

        // Populate fields with existing item data
        itemNameEditText.setText(itemName)
        itemQuantityEditText.setText(itemQuantity.toString())
        itemImageView.setImageResource(itemImageResId)

        // Handle save button click
        saveButton.setOnClickListener {
            saveUpdatedItem()
        }
    }

    private fun saveUpdatedItem() {
        val updatedName = itemNameEditText.text.toString()
        val updatedQuantity = itemQuantityEditText.text.toString().toIntOrNull() ?: 0

        // Prepare result intent
        val resultIntent = Intent().apply {
            putExtra("updated_item_id", itemId)
            putExtra("updated_item_name", updatedName)
            putExtra("updated_item_quantity", updatedQuantity)
            putExtra("updated_item_image", itemImageResId)
        }

        // Send result back and finish activity
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}