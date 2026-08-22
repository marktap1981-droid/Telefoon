package nl.voorraadbeheer.app.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json?fields=product_name,brands,image_front_url,image_url,categories")
    suspend fun getProduct(@Path("barcode") barcode: String): OpenFoodFactsResponse
}

@Serializable
data class OpenFoodFactsResponse(
    val status: Int = 0,
    val product: OpenFoodFactsProduct? = null,
)

@Serializable
data class OpenFoodFactsProduct(
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    @SerialName("image_front_url") val imageFrontUrl: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val categories: String? = null,
)
