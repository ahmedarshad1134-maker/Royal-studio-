package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.repository.RepositoryProvider
import com.example.ui.models.MediaType
import com.example.ui.models.PortfolioCategory
import com.example.ui.models.PortfolioItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PortfolioUiState(
    val isLoading: Boolean = true,
    val items: List<PortfolioItem> = emptyList(),
    val filteredItems: List<PortfolioItem> = emptyList(),
    val selectedCategory: PortfolioCategory = PortfolioCategory.ALL,
    val selectedItemForPreview: PortfolioItem? = null
)

class PortfolioViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()
    
    private val repository = RepositoryProvider.firebaseRepository

    init {
        loadPortfolioItems()
    }

    private fun loadPortfolioItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (repository.isInitialized) {
                repository.getPortfolioItems().collect { dtoList ->
                    if (dtoList.isEmpty()) {
                        loadMockData()
                    } else {
                        val items = dtoList.filter { it.enabled }.map { dto ->
                            PortfolioItem(
                                id = dto.id,
                                title = dto.title,
                                category = mapStringToCategory(dto.category),
                                type = if (dto.mediaType == "VIDEO") MediaType.VIDEO else MediaType.IMAGE,
                                imageUrl = dto.mediaUrl.takeIf { it.isNotBlank() },
                                imageResId = R.drawable.portfolio_sample_1789061922601, // fallback
                                featured = dto.featured,
                                createdAt = dto.createdAt?.time ?: 0,
                                isBroken = false
                            )
                        }
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                items = items,
                                filteredItems = filterItems(items, state.selectedCategory)
                            )
                        }
                    }
                }
            } else {
                loadMockData()
            }
        }
    }
    
    private fun mapStringToCategory(str: String): PortfolioCategory {
        return PortfolioCategory.values().find { it.name == str } ?: PortfolioCategory.OTHER_EVENTS
    }

    private suspend fun loadMockData() {
        delay(1200)
        val mockItems = listOf(
            PortfolioItem("1", "Golden Hour Vows", PortfolioCategory.WEDDING, MediaType.IMAGE, null, R.drawable.hero_wedding_cinematic_1789061882938, true, 1690000000),
            PortfolioItem("2", "The Perfect Ring", PortfolioCategory.ENGAGEMENT, MediaType.IMAGE, null, R.drawable.service_wedding_1789061903404, false, 1690000001),
            PortfolioItem("3", "Sunset Silhouette", PortfolioCategory.PRE_WEDDING, MediaType.IMAGE, null, R.drawable.portfolio_sample_1789061922601, true, 1690000002),
            PortfolioItem("4", "Cinematic Highlight", PortfolioCategory.WEDDING, MediaType.VIDEO, null, R.drawable.hero_wedding_cinematic_1789061882938, true, 1690000003),
            PortfolioItem("5", "Luxury Gala", PortfolioCategory.OTHER_EVENTS, MediaType.IMAGE, null, R.drawable.service_event_1789061936720, false, 1690000004),
            PortfolioItem("6", "Sweet Sixteen", PortfolioCategory.BIRTHDAY, MediaType.IMAGE, null, R.drawable.portfolio_birthday_1789062228988, true, 1690000005),
            PortfolioItem("7", "A New Beginning", PortfolioCategory.BABY_FAMILY, MediaType.IMAGE, null, R.drawable.portfolio_baby_1789062242119, true, 1690000006),
            PortfolioItem("8", "Silver Jubilee", PortfolioCategory.ANNIVERSARY, MediaType.IMAGE, null, R.drawable.service_event_1789061936720, false, 1690000007),
            PortfolioItem("9", "Broken Media Test", PortfolioCategory.OTHER_EVENTS, MediaType.IMAGE, null, 0, false, 1690000008, true)
        )
        _uiState.update { 
            it.copy(
                isLoading = false,
                items = mockItems,
                filteredItems = filterItems(mockItems, it.selectedCategory)
            )
        }
    }

    fun selectCategory(category: PortfolioCategory) {
        _uiState.update { state ->
            state.copy(
                selectedCategory = category,
                filteredItems = filterItems(state.items, category)
            )
        }
    }
    
    fun openPreview(item: PortfolioItem) {
        _uiState.update { it.copy(selectedItemForPreview = item) }
    }
    
    fun closePreview() {
        _uiState.update { it.copy(selectedItemForPreview = null) }
    }

    private fun filterItems(items: List<PortfolioItem>, category: PortfolioCategory): List<PortfolioItem> {
        return if (category == PortfolioCategory.ALL) {
            items
        } else {
            items.filter { it.category == category }
        }
    }
}
