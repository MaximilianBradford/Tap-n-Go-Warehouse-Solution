package dev.tapngo.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.tapngo.app.R
import dev.tapngo.app.adapter.InventoryAdapter
import dev.tapngo.app.model.InventoryItem

class InventoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private val inventoryList = mutableListOf<InventoryItem>()  // ✅ Mutable list for updates

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ✅ Initialize inventory list with sample data
        inventoryList.addAll(
            listOf(
                InventoryItem(1, "Laptop", 5, R.drawable.ic_laptop),
                InventoryItem(2, "Mouse", 15, R.drawable.ic_mouse),
                InventoryItem(3, "Keyboard", 10, R.drawable.ic_keyboard)
            )
        )

        inventoryAdapter = InventoryAdapter(inventoryList) { item: InventoryItem ->
            openEditInventoryActivity(item)
        }


        recyclerView.adapter = inventoryAdapter
    }

    // ✅ Open EditInventoryActivity for editing
    private fun openEditInventoryActivity(item: InventoryItem) {
        val intent = Intent(this, EditInventoryActivity::class.java).apply {
            putExtra("item_id", item.id)
            putExtra("item_name", item.name)
            putExtra("item_quantity", item.quantity)
            putExtra("item_image", item.imageResId)
        }
        startActivityForResult(intent, EDIT_ITEM_REQUEST_CODE)
    }

    // ✅ Handle result from EditInventoryActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ITEM_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val updatedId = data.getIntExtra("updated_item_id", -1)
            val updatedName = data.getStringExtra("updated_item_name") ?: ""
            val updatedQuantity = data.getIntExtra("updated_item_quantity", 0)
            val updatedImage = data.getIntExtra("updated_item_image", R.drawable.ic_placeholder)

            val index = inventoryList.indexOfFirst { it.id == updatedId }
            if (index != -1) {
                inventoryList[index] =
                    InventoryItem(updatedId, updatedName, updatedQuantity, updatedImage)
                inventoryAdapter.notifyItemChanged(index)
            }
        }
    }

    companion object {
        private const val EDIT_ITEM_REQUEST_CODE = 100
    }
}