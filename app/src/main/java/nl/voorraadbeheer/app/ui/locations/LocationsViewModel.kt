package nl.voorraadbeheer.app.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.Location
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import javax.inject.Inject

data class LocationsUiState(
    val locations: List<Location> = emptyList(),
    val itemCounts: Map<String, Int> = emptyMap(),
)

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    inventoryRepository: InventoryRepository,
) : ViewModel() {

    val uiState: StateFlow<LocationsUiState> = combine(
        locationRepository.observeLocations(),
        inventoryRepository.observeAllItems(),
    ) { locations, items ->
        LocationsUiState(
            locations = locations,
            itemCounts = items.groupingBy { it.locationId }.eachCount(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationsUiState())

    fun addLocation(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { locationRepository.addLocation(name.trim()) }
    }

    /** Verwijdert de locatie alleen als er geen voorraad meer aan hangt. */
    fun deleteLocation(locationId: String) {
        if ((uiState.value.itemCounts[locationId] ?: 0) > 0) return
        viewModelScope.launch { locationRepository.deleteLocation(locationId) }
    }
}
