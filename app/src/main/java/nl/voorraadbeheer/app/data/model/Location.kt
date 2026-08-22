package nl.voorraadbeheer.app.data.model

import com.google.firebase.firestore.DocumentId

data class Location(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val colorHex: String = "#2E7D32",
)

/** Standaardlocaties die worden aangemaakt zodra een gebruiker voor het eerst inlogt. */
val DEFAULT_LOCATIONS = listOf(
    "Kast" to "#2E7D32",
    "Koelkast" to "#1976D2",
    "Camping" to "#F9A825",
)
