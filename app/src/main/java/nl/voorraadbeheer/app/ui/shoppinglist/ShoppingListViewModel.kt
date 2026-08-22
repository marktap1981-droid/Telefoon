package nl.voorraadbeheer.app.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nl.voorraadbeheer.app.data.model.InventoryItem
import nl.voorraadbeheer.app.data.model.ShoppingListItem
import nl.voorraadbeheer.app.data.repository.InventoryRepository
import nl.voorraadbeheer.app.data.repository.ShoppingListRepository
import javax.inject.Inject

data class ShoppingListUiState(
    val lowStockItems: List<InventoryItem> = emptyList(),
    val manualItems: List<ShoppingListItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    inventoryRepository: InventoryRepository,
    private val shoppingListRepository: ShoppingListRepository,
) : ViewModel() {

    val uiState: StateFlow<ShoppingListUiState> = combine(
        inventoryRepository.observeAllItems(),
        shoppingListRepository.observeManualItems(),
    ) { items, manualItems ->
        ShoppingListUiState(
            lowStockItems = items.filter { it.isLowStock },
            manualItems = manualItems,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShoppingListUiState())

    fun addManualItem(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { shoppingListRepository.addManualItem(name.trim()) }
    }

    fun setChecked(itemId: String, checked: Boolean) {
        viewModelScope.launch { shoppingListRepository.setChecked(itemId, checked) }
    }

    fun deleteManualItem(itemId: String) {
        viewModelScope.launch { shoppingListRepository.deleteManualItem(itemId) }
    }
}
