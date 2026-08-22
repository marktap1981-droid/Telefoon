package nl.voorraadbeheer.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/** Handmatig toegevoegd boodschappenlijst-item. Items door lage voorraad worden live afgeleid van InventoryItem. */
data class ShoppingListItem(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val checked: Boolean = false,
    val addedAt: Timestamp? = null,
)
