package nl.voorraadbeheer.app.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.Location
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val inventoryRepository: InventoryRepository,
) : ViewModel() {

    val locations: StateFlow<List<Location>> = locationRepository.observeLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLocation(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { locationRepository.addLocation(name.trim()) }
    }

    fun deleteLocation(locationId: String) {
        viewModelScope.launch {
            inventoryRepository.deleteItemsForLocation(locationId)
            locationRepository.deleteLocation(locationId)
        }
    }
}
