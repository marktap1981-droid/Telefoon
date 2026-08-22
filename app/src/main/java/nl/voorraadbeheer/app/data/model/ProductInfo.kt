package nl.voorraadbeheer.app.data.model

/** Productinformatie zoals opgehaald van Open Food Facts (of uit de gedeelde Firestore-cache). */
data class ProductInfo(
    val barcode: String = "",
    val name: String = "",
    val brand: String = "",
    val imageUrl: String = "",
    val category: String = "",
) {
    val isEmpty: Boolean
        get() = name.isBlank()
}
