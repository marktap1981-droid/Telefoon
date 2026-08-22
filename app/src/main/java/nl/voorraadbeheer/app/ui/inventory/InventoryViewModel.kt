package nl.voorraadbeheer.app.ui.inventory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.data.model.Location
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import nl.voorraadbeheer.app.ui.navigation.Routes
import javax.inject.Inject

data class InventoryUiState(
    val items: List<InventoryItem> = emptyList(),
    val locations: List<Location> = emptyList(),
    val selectedLocationId: String? = null,
    val query: String = "",
    val isLoading: Boolean = true,
) {
    val filteredItems: List<InventoryItem>
        get() = items
            .filter { selectedLocationId.isNullOrBlank() || it.locationId == selectedLocationId }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    locationRepository: LocationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialLocationId: String? =
        savedStateHandle.get<String>(Routes.INVENTORY_ARG_LOCATION_ID)?.takeIf { it.isNotBlank() }

    private val selectedLocationId = MutableStateFlow(initialLocationId)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<InventoryUiState> = combine(
        inventoryRepository.observeAllItems(),
        locationRepository.observeLocations(),
        selectedLocationId,
        query,
    ) { items, locations, selected, q ->
        InventoryUiState(
            items = items,
            locations = locations,
            selectedLocationId = selected,
            query = q,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryUiState())

    fun selectLocation(locationId: String?) {
        selectedLocationId.value = locationId
    }

    fun setQuery(value: String) {
        query.value = value
    }

    fun changeQuantity(item: InventoryItem, delta: Int) {
        viewModelScope.launch {
            inventoryRepository.updateQuantity(item.id, item.quantity + delta)
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch { inventoryRepository.deleteItem(itemId) }
    }
}
