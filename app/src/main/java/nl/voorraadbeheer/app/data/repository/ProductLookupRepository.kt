package nl.voorraadbeheer.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import nl.voorraadbeheer.app.data.model.ProductInfo
import nl.voorraadbeheer.app.data.remote.OpenFoodFactsApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductLookupRepository @Inject constructor(
    private val api: OpenFoodFactsApi,
    private val firestore: FirebaseFirestore,
) {
    private fun cacheDoc(barcode: String) = firestore.collection("products").document(barcode)

    /**
     * Zoekt productinfo op basis van barcode: eerst in de gedeelde Firestore-cache,
     * anders via de Open Food Facts API (en cachet het resultaat voor volgende scans).
     */
    suspend fun lookup(barcode: String): ProductInfo? {
        val cached = runCatching { cacheDoc(barcode).get().await() }.getOrNull()
        if (cached != null && cached.exists()) {
            return cached.toObject(ProductInfo::class.java)
        }

        val response = runCatching { api.getProduct(barcode) }.getOrNull() ?: return null
        val product = response.product ?: return null
        val name = product.productName?.trim().orEmpty()
        if (name.isBlank()) return null

        val info = ProductInfo(
            barcode = barcode,
            name = name,
            brand = product.brands?.trim().orEmpty(),
            imageUrl = product.imageFrontUrl ?: product.imageUrl.orEmpty(),
            category = product.categories?.trim()?.substringBefore(",").orEmpty(),
        )

        runCatching { cacheDoc(barcode).set(info).await() }
        return info
    }
}
