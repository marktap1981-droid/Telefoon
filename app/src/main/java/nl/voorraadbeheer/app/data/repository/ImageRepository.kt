package nl.voorraadbeheer.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val householdRepository: HouseholdRepository,
    @ApplicationContext private val context: Context,
) {
    /** Uploadt een door de gebruiker gekozen foto en geeft de openbare download-URL terug. */
    suspend fun uploadProductImage(imageUri: Uri): String {
        val householdId = householdRepository.getHouseholdId()
        val ref = storage.reference
            .child("households/$householdId/product_images/${UUID.randomUUID()}.jpg")

        context.contentResolver.openInputStream(imageUri)?.use { stream ->
            ref.putStream(stream).await()
        } ?: error("Kon de gekozen foto niet lezen")

        return ref.downloadUrl.await().toString()
    }
}
