package nl.voorraadbeheer.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class InventoryItem(
    @DocumentId
    val id: String = "",
    val barcode: String? = null,
    val name: String = "",
    val brand: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val locationId: String = "",
    val quantity: Int = 0,
    val minQuantity: Int = 0,
    val expiryDate: Timestamp? = null,
    val addedAt: Timestamp? = null,
) {
    val isLowStock: Boolean
        get() = minQuantity > 0 && quantity <= minQuantity
}
