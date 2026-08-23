package nl.voorraadbeheer.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.InventoryItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private fun itemsCollection() =
        firestore.collection("users").document(requireUid()).collection("inventoryItems")

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Gebruiker is niet ingelogd")

    /** Live-lijst van alle voorraaditems van de gebruiker, over alle locaties heen. */
    fun observeAllItems(): Flow<List<InventoryItem>> = callbackFlow {
        val registration = itemsCollection()
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(InventoryItem::class.java).orEmpty())
            }
        awaitClose { registration.remove() }
    }

    /** Live-status van één voorraaditem, voor het detailscherm. Null zodra het item niet (meer) bestaat. */
    fun observeItem(itemId: String): Flow<InventoryItem?> = callbackFlow {
        val registration = itemsCollection().document(itemId)
            .addSnapshotListener { snapshot, _ ->
                trySend(if (snapshot != null && snapshot.exists()) snapshot.toObject(InventoryItem::class.java) else null)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addItem(item: InventoryItem) {
        val withTimestamp = item.copy(addedAt = Timestamp.now())
        itemsCollection().add(withTimestamp).await()
    }

    suspend fun updateItem(item: InventoryItem) {
        require(item.id.isNotBlank()) { "Item heeft geen id" }
        itemsCollection().document(item.id).set(item).await()
    }

    suspend fun updateQuantity(itemId: String, newQuantity: Int) {
        itemsCollection().document(itemId)
            .update("quantity", newQuantity.coerceAtLeast(0))
            .await()
    }

    suspend fun updateMinQuantity(itemId: String, newMinQuantity: Int) {
        itemsCollection().document(itemId)
            .update("minQuantity", newMinQuantity.coerceAtLeast(0))
            .await()
    }

    suspend fun deleteItem(itemId: String) {
        itemsCollection().document(itemId).delete().await()
    }

    suspend fun deleteItemsForLocation(locationId: String) {
        val batch = firestore.batch()
        val items = itemsCollection().whereEqualTo("locationId", locationId).get().await()
        items.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}
