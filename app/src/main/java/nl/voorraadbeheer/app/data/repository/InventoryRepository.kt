package nl.voorraadbeheer.app.data.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.widget.LowStockWidget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val householdRepository: HouseholdRepository,
    @ApplicationContext private val context: Context,
) {
    private suspend fun itemsCollection() =
        firestore.collection("households").document(householdRepository.getHouseholdId()).collection("inventoryItems")

    private suspend fun refreshWidget() {
        runCatching { LowStockWidget.updateAll(context) }
    }

    /** Live-lijst van alle voorraaditems van het huishouden, over alle locaties heen. */
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

    /** Zoekt een bestaand voorraaditem met deze barcode (ongeacht locatie), voor scan-van-bestaand-product. */
    suspend fun findByBarcode(barcode: String): InventoryItem? {
        val snapshot = itemsCollection().whereEqualTo("barcode", barcode).limit(1).get().await()
        return snapshot.documents.firstOrNull()?.toObject(InventoryItem::class.java)
    }

    suspend fun addItem(item: InventoryItem) {
        val withTimestamp = item.copy(addedAt = Timestamp.now())
        itemsCollection().add(withTimestamp).await()
        refreshWidget()
    }

    suspend fun updateItem(item: InventoryItem) {
        require(item.id.isNotBlank()) { "Item heeft geen id" }
        itemsCollection().document(item.id).set(item).await()
        refreshWidget()
    }

    suspend fun updateQuantity(itemId: String, newQuantity: Int) {
        itemsCollection().document(itemId)
            .update("quantity", newQuantity.coerceAtLeast(0))
            .await()
        refreshWidget()
    }

    suspend fun updateMinQuantity(itemId: String, newMinQuantity: Int) {
        itemsCollection().document(itemId)
            .update("minQuantity", newMinQuantity.coerceAtLeast(0))
            .await()
        refreshWidget()
    }

    suspend fun deleteItem(itemId: String) {
        itemsCollection().document(itemId).delete().await()
        refreshWidget()
    }
}
