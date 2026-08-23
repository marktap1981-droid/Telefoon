package nl.voorraadbeheer.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789" // zonder 0/O/1/I/L, minder verwarring bij delen

/**
 * Koppelt een ingelogde gebruiker aan een "huishouden": de gedeelde container waarin
 * locaties/voorraad/boodschappenlijst leven, zodat meerdere mensen (bv. partners) dezelfde
 * gegevens zien. Nieuwe gebruikers krijgen automatisch hun eigen huishouden; ze kunnen zich
 * daarna aansluiten bij een bestaand huishouden via een deelcode.
 */
@Singleton
class HouseholdRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {
    private val mutex = Mutex()
    private var cachedHouseholdId: String? = null

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Gebruiker is niet ingelogd")

    private fun userDoc() = firestore.collection("users").document(requireUid())
    private fun householdsCollection() = firestore.collection("households")

    /** Zorgt dat de gebruiker een huishouden heeft; maakt er zo nodig één aan. Idempotent. */
    suspend fun ensureHousehold(): String {
        val existing = userDoc().get().await().getString("householdId")
        if (existing != null) return existing

        val code = generateUniqueCode()
        val householdRef = householdsCollection().document()
        householdRef.set(
            mapOf(
                "code" to code,
                "memberUids" to listOf(requireUid()),
                "createdAt" to com.google.firebase.Timestamp.now(),
            ),
        ).await()
        userDoc().set(mapOf("householdId" to householdRef.id)).await()
        return householdRef.id
    }

    /** Resolvet en cachet het huishouden-id voor de rest van deze app-sessie. */
    suspend fun getHouseholdId(): String = mutex.withLock {
        cachedHouseholdId?.let { return@withLock it }
        val id = ensureHousehold()
        cachedHouseholdId = id
        id
    }

    /** Deelcode van het huidige huishouden, om te delen met bv. een partner. */
    suspend fun getShareCode(): String {
        val householdId = getHouseholdId()
        return householdsCollection().document(householdId).get().await().getString("code").orEmpty()
    }

    /**
     * Sluit je aan bij een bestaand huishouden op basis van de deelcode.
     * Vervangt je eigen huishouden-koppeling; herstart van de app is nodig om de nieuwe
     * data te laden (repositories cachen het huishouden-id voor de sessie).
     */
    suspend fun joinHousehold(code: String): Boolean {
        val normalized = code.trim().uppercase()
        val match = householdsCollection().whereEqualTo("code", normalized).limit(1).get().await()
        val doc = match.documents.firstOrNull() ?: return false

        doc.reference.update("memberUids", FieldValue.arrayUnion(requireUid())).await()
        userDoc().set(mapOf("householdId" to doc.id)).await()
        mutex.withLock { cachedHouseholdId = doc.id }
        return true
    }

    private suspend fun generateUniqueCode(): String {
        repeat(10) {
            val candidate = (1..6).map { CODE_CHARS.random() }.joinToString("")
            val existing = householdsCollection().whereEqualTo("code", candidate).limit(1).get().await()
            if (existing.isEmpty) return candidate
        }
        error("Kon geen unieke huishouden-code genereren")
    }
}
