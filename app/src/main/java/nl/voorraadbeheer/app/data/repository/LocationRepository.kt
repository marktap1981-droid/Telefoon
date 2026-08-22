package nl.voorraadbeheer.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.DEFAULT_LOCATIONS
import nl.voorraadbeheer.app.data.model.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private fun locationsCollection() =
        firestore.collection("users").document(requireUid()).collection("locations")

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Gebruiker is niet ingelogd")

    fun observeLocations(): Flow<List<Location>> = callbackFlow {
        val registration = locationsCollection()
            .orderBy("name")
            .addSnapshotListener { snapshot, _ ->
                val locations = snapshot?.toObjects(Location::class.java).orEmpty()
                trySend(locations)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addLocation(name: String, colorHex: String = "#2E7D32") {
        val location = Location(name = name, colorHex = colorHex)
        locationsCollection().add(location).await()
    }

    suspend fun deleteLocation(locationId: String) {
        locationsCollection().document(locationId).delete().await()
    }

    suspend fun ensureDefaultLocationsExist() {
        val existing = locationsCollection().get().await()
        if (!existing.isEmpty) return
        DEFAULT_LOCATIONS.forEach { (name, color) ->
            addLocation(name, color)
        }
    }
}
