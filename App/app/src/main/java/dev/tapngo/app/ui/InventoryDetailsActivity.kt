package dev.tapngo.app.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dev.tapngo.app.R

class InventoryDetailsActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_details)

        val itemName = intent.getStringExtra("item_name")
        val itemQuantity = intent.getIntExtra("item_quantity", 0)
        val itemImage = intent.getIntExtra("item_image", R.drawable.ic_placeholder)

        findViewById<TextView>(R.id.itemNameTextView).text = itemName
        findViewById<TextView>(R.id.itemQuantityTextView).text = "Quantity: $itemQuantity"
        findViewById<ImageView>(R.id.itemImageView).setImageResource(itemImage)
    }
}