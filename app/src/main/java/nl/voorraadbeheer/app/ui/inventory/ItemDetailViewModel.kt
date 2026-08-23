package nl.voorraadbeheer.app.ui.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import nl.voorraadbeheer.app.ui.navigation.Routes
import javax.inject.Inject

data class ItemDetailUiState(
    val item: InventoryItem? = null,
    val locationName: String = "",
    val isLoading: Boolean = true,
)

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val itemId: String =
        checkNotNull(savedStateHandle.get<String>(Routes.ITEM_DETAIL_ARG_ID))

    val uiState: StateFlow<ItemDetailUiState> = combine(
        inventoryRepository.observeItem(itemId),
        locationRepository.observeLocations(),
    ) { item, locations ->
        ItemDetailUiState(
            item = item,
            locationName = locations.firstOrNull { it.id == item?.locationId }?.name.orEmpty(),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ItemDetailUiState())

    fun changeQuantity(delta: Int) {
        val item = uiState.value.item ?: return
        viewModelScope.launch {
            inventoryRepository.updateQuantity(item.id, item.quantity + delta)
        }
    }

    fun deleteItem() {
        val item = uiState.value.item ?: return
        viewModelScope.launch {
            inventoryRepository.deleteItem(item.id)
        }
    }
}
