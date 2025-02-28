package dev.tapngo.app.utils.inventreeutils.components

class Location(
    val id: Int,
    val name: String,
    val description: String?,
    val path: String,
    val items: Int?,
    val sublocations: Int?
) {
    private var quantity: Int? = null
    private var pk: Int? = null

    fun getQuantity(): Int? {
        return quantity
    }

    fun setQuantity(quantity: Int) {
        this.quantity = quantity
    }

    fun getPk(): Int? {
        return pk
    }

    fun setPk(pk: Int) {
        this.pk = pk
    }
}