package nl.voorraadbeheer.app.ui.addproduct

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.data.model.Location
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import nl.voorraadbeheer.app.data.repository.ProductLookupRepository
import nl.voorraadbeheer.app.ui.navigation.Routes
import javax.inject.Inject

data class AddEditProductUiState(
    val barcode: String? = null,
    val name: String = "",
    val brand: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val locationId: String = "",
    val quantity: Int = 1,
    val minQuantity: Int = 0,
    val expiryDateMillis: Long? = null,
    val locations: List<Location> = emptyList(),
    val isLookingUp: Boolean = false,
    val lookupFailed: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class AddEditProductViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productLookupRepository: ProductLookupRepository,
    locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val barcodeArg: String? =
        savedStateHandle.get<String>(Routes.ADD_PRODUCT_ARG_BARCODE)?.takeIf { it.isNotBlank() }

    private val _uiState = MutableStateFlow(AddEditProductUiState(barcode = barcodeArg))
    val uiState: StateFlow<AddEditProductUiState> = _uiState.asStateFlow()

    init {
        if (barcodeArg != null) {
            lookupProduct(barcodeArg)
        }
        viewModelScope.launch {
            locationRepository.observeLocations().collect { locations ->
                _uiState.update { state ->
                    state.copy(
                        locations = locations,
                        locationId = state.locationId.ifBlank { locations.firstOrNull()?.id.orEmpty() },
                    )
                }
            }
        }
    }

    private fun lookupProduct(barcode: String) {
        _uiState.value = _uiState.value.copy(isLookingUp = true, lookupFailed = false)
        viewModelScope.launch {
            val info = runCatching { productLookupRepository.lookup(barcode) }.getOrNull()
            _uiState.value = if (info != null && !info.isEmpty) {
                _uiState.value.copy(
                    isLookingUp = false,
                    name = info.name,
                    brand = info.brand,
                    imageUrl = info.imageUrl,
                    category = info.category,
                )
            } else {
                _uiState.value.copy(isLookingUp = false, lookupFailed = true)
            }
        }
    }

    fun updateName(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun updateBrand(value: String) { _uiState.value = _uiState.value.copy(brand = value) }
    fun updateCategory(value: String) { _uiState.value = _uiState.value.copy(category = value) }
    fun updateLocation(locationId: String) { _uiState.value = _uiState.value.copy(locationId = locationId) }
    fun updateQuantity(value: Int) { _uiState.value = _uiState.value.copy(quantity = value.coerceAtLeast(0)) }
    fun updateMinQuantity(value: Int) { _uiState.value = _uiState.value.copy(minQuantity = value.coerceAtLeast(0)) }
    fun updateExpiryMillis(value: Long?) { _uiState.value = _uiState.value.copy(expiryDateMillis = value) }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.locationId.isBlank()) return
        viewModelScope.launch {
            val item = InventoryItem(
                barcode = state.barcode,
                name = state.name.trim(),
                brand = state.brand.trim(),
                imageUrl = state.imageUrl,
                category = state.category.trim(),
                locationId = state.locationId,
                quantity = state.quantity,
                minQuantity = state.minQuantity,
                expiryDate = state.expiryDateMillis?.let { Timestamp(java.util.Date(it)) },
            )
            inventoryRepository.addItem(item)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
