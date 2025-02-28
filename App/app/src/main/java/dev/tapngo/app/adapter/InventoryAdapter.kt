package dev.tapngo.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.tapngo.app.R
import dev.tapngo.app.model.InventoryItem // ✅ Missing import added

class InventoryAdapter(
    private val items: MutableList<InventoryItem>, // ✅ Changed to MutableList for dynamic updates
    private val onEditClick: (InventoryItem) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.itemImage)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemQuantity: TextView = view.findViewById(R.id.itemQuantity)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent, false) // Uses your item layout
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = items[position]
        holder.itemImage.setImageResource(item.imageResId)
        holder.itemName.text = item.name
        holder.itemQuantity.text = "Quantity: ${item.quantity}"

        holder.editButton.setOnClickListener {
            onEditClick(item)
        }
    }

    override fun getItemCount() = items.size

    // ✅ Function to update an item after editing
    fun updateItem(updatedItem: InventoryItem) {
        val index = items.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            items[index] = updatedItem
            notifyItemChanged(index)
        }
    }
}
