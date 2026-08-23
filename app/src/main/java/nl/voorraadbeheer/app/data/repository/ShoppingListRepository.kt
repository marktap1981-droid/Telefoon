package nl.voorraadbeheer.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.ShoppingListItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val householdRepository: HouseholdRepository,
) {
    private suspend fun listCollection() =
        firestore.collection("households").document(householdRepository.getHouseholdId()).collection("shoppingList")

    fun observeManualItems(): Flow<List<ShoppingListItem>> = callbackFlow {
        val registration = listCollection()
            .orderBy("addedAt")
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.toObjects(ShoppingListItem::class.java).orEmpty())
            }
        awaitClose { registration.remove() }
    }

    suspend fun addManualItem(name: String) {
        listCollection().add(ShoppingListItem(name = name, addedAt = Timestamp.now())).await()
    }

    suspend fun setChecked(itemId: String, checked: Boolean) {
        listCollection().document(itemId).update("checked", checked).await()
    }

    suspend fun deleteManualItem(itemId: String) {
        listCollection().document(itemId).delete().await()
    }
}
