package nl.voorraadbeheer.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.data.model.Location
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.LocationRepository
import nl.voorraadbeheer.app.data.repository.UserPreferencesRepository
import nl.voorraadbeheer.app.util.daysUntil
import javax.inject.Inject

data class LocationSummary(val location: Location, val itemCount: Int)

data class DashboardUiState(
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val locationSummaries: List<LocationSummary> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    locationRepository: LocationRepository,
    preferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        inventoryRepository.observeAllItems(),
        locationRepository.observeLocations(),
        preferencesRepository.preferences,
    ) { items: List<InventoryItem>, locations: List<Location>, prefs ->
        val lowStock = items.count { it.isLowStock }
        val expiringSoon = items.count { item ->
            val expiry = item.expiryDate ?: return@count false
            val days = expiry.daysUntil()
            days in 0..prefs.expiryReminderDays.toLong()
        }
        val summaries = locations.map { location ->
            LocationSummary(location, items.count { it.locationId == location.id })
        }
        DashboardUiState(
            totalProducts = items.size,
            lowStockCount = lowStock,
            expiringSoonCount = expiringSoon,
            locationSummaries = summaries,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
