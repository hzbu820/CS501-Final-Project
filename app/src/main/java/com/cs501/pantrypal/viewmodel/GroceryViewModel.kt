package com.cs501.pantrypal.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.GroceryItem
import com.cs501.pantrypal.data.repository.GroceryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Grocery ViewModel - Completely rewritten for reliability
 */
class GroceryViewModel(application: Application) : BaseViewModel(application) {
    private val repository: GroceryRepository
    
    // Main state flow for UI
    private val _uiState = MutableStateFlow(GroceryUiState())
    val allGroceryItems: StateFlow<List<GroceryItem>> = _uiState.asStateFlow().let { flow ->
        MutableStateFlow(emptyList<GroceryItem>()).apply {
            viewModelScope.launch {
                flow.collect { state ->
                    this@apply.value = state.displayedItems
                }
            }
        }
    }
    
    // StateFlow for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // StateFlow for showing checked items
    private val _showCheckedItems = MutableStateFlow(true)
    val showCheckedItems: StateFlow<Boolean> = _showCheckedItems.asStateFlow()
    
    // StateFlow for category filter
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()
    
    // Track the active collection job so we can cancel it when needed
    private var activeCollectionJob: Job? = null
    
    // Data class to represent the complete UI state
    data class GroceryUiState(
        val allItems: List<GroceryItem> = emptyList(),
        val displayedItems: List<GroceryItem> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = GroceryRepository(database.groceryItemDao())
        loadItems()
    }
    
    override fun onUserIdChanged(userId: String) {
        loadItems()
    }
    
    /**
     * Main function to load and display items based on current filters
     */
    private fun loadItems() {
        // Cancel any active collection job to prevent conflicts
        activeCollectionJob?.cancel()
        
        // Start a new collection
        activeCollectionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Determine which repository method to use based on filters
                val flow = when {
                    _searchQuery.value.isNotBlank() -> {
                        repository.searchGroceryItems(getCurrentUserId(), _searchQuery.value)
                    }
                    _categoryFilter.value != null -> {
                        repository.getGroceryItemsByCategory(
                            getCurrentUserId(), 
                            _categoryFilter.value!!, 
                            true // Always get all items and filter in-memory
                        )
                    }
                    else -> {
                        repository.getAllGroceryItemsByUserId(getCurrentUserId())
                    }
                }
                
                // Collect the latest emissions from the flow
                flow.catch { e ->
                    Log.e("GroceryViewModel", "Error loading items: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load items: ${e.message}"
                    )
                }.collectLatest { items ->
                    Log.d("GroceryViewModel", "Received ${items.size} items from repository")
                    
                    // Apply filters and sorting
                    val filteredItems = applyFilters(items)
                    val sortedItems = sortItems(filteredItems)
                    
                    // Update state with all items and filtered/sorted items
                    _uiState.value = _uiState.value.copy(
                        allItems = items,
                        displayedItems = sortedItems,
                        isLoading = false,
                        error = null
                    )
                    
                    Log.d("GroceryViewModel", "Updated UI with ${sortedItems.size} items")
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error in loadItems: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading items: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Apply filters to the items based on current settings
     */
    private fun applyFilters(items: List<GroceryItem>): List<GroceryItem> {
        return items.filter { item ->
            // If "Show completed" is OFF, hide checked items
            (_showCheckedItems.value || !item.isChecked) &&
            // Apply category filter if set
            (_categoryFilter.value == null || item.category == _categoryFilter.value)
        }
    }
    
    /**
     * Sort items consistently to prevent jumping
     */
    private fun sortItems(items: List<GroceryItem>): List<GroceryItem> {
        return items.sortedWith(
            compareBy<GroceryItem> { 
                // Try to convert name to a number for numerical sorting
                val nameAsNumber = it.name.toIntOrNull()
                if (nameAsNumber != null) {
                    nameAsNumber
                } else {
                    Int.MAX_VALUE // Put non-numeric names after numeric ones
                }
            }
            .thenBy { it.name.lowercase() }
            .thenBy { it.id ?: it.name } // Fallback to name if id is null
        )
    }
    
    /**
     * Toggle whether completed items are shown
     */
    fun toggleShowCheckedItems() {
        _showCheckedItems.value = !_showCheckedItems.value
        Log.d("GroceryViewModel", "Toggled show checked items to: ${_showCheckedItems.value}")
        
        // Don't fetch from repository again, just apply filters to the items we already have
        val currentItems = _uiState.value.allItems
        val filteredItems = applyFilters(currentItems)
        val sortedItems = sortItems(filteredItems)
        
        _uiState.value = _uiState.value.copy(displayedItems = sortedItems)
    }
    
    /**
     * Set category filter
     */
    fun setCategoryFilter(category: String?) {
        if (_categoryFilter.value == category) return
        
        _categoryFilter.value = category
        
        // If changing to/from a category, reload items
        // Otherwise just apply filters to existing items
        if (category != null) {
            loadItems()
        } else {
            val currentItems = _uiState.value.allItems
            val filteredItems = applyFilters(currentItems)
            val sortedItems = sortItems(filteredItems)
            
            _uiState.value = _uiState.value.copy(displayedItems = sortedItems)
        }
    }
    
    /**
     * Search for grocery items by name
     */
    fun searchGroceryItems(query: String) {
        _searchQuery.value = query
        loadItems()
    }
    
    /**
     * Add a new grocery item
     */
    fun addGroceryItem(name: String, quantity: String, unit: String, category: String) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            try {
                val groceryItem = GroceryItem(
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    category = category,
                    userId = getCurrentUserId()
                )
                repository.insertGroceryItem(groceryItem)
                
                // Reload all items after adding
                loadItems()
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error adding item: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add item: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Toggle checked state of a grocery item
     */
    fun toggleItemChecked(groceryItem: GroceryItem) {
        viewModelScope.launch {
            try {
                // Create updated item with toggled checked state
                val updatedItem = groceryItem.copy(isChecked = !groceryItem.isChecked)
                Log.d("GroceryViewModel", "Toggling item ${groceryItem.name} from ${groceryItem.isChecked} to ${updatedItem.isChecked}")
                
                // Update in database
                repository.updateGroceryItem(updatedItem)
                
                // Update in local state immediately to avoid UI delay
                val currentItems = _uiState.value.allItems.toMutableList()
                val index = currentItems.indexOfFirst { it.id == updatedItem.id }
                
                if (index >= 0) {
                    // Update the item in our local cache
                    currentItems[index] = updatedItem
                    
                    // Apply filters
                    val filteredItems = applyFilters(currentItems)
                    val sortedItems = sortItems(filteredItems)
                    
                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        allItems = currentItems,
                        displayedItems = sortedItems
                    )
                    
                    Log.d("GroceryViewModel", "Updated item in local state, displaying ${sortedItems.size} items")
                } else {
                    // If item wasn't found in our local cache, reload all items
                    Log.d("GroceryViewModel", "Item not found in local cache, reloading all items")
                    loadItems()
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error toggling item: ${e.message}", e)
                loadItems() // Reload items to recover from error
            }
        }
    }
    
    /**
     * Update a grocery item
     */
    fun updateGroceryItem(groceryItem: GroceryItem) {
        viewModelScope.launch {
            try {
                repository.updateGroceryItem(groceryItem)
                
                // Update in local state
                val currentItems = _uiState.value.allItems.toMutableList()
                val index = currentItems.indexOfFirst { it.id == groceryItem.id }
                
                if (index >= 0) {
                    currentItems[index] = groceryItem
                    
                    val filteredItems = applyFilters(currentItems)
                    val sortedItems = sortItems(filteredItems)
                    
                    _uiState.value = _uiState.value.copy(
                        allItems = currentItems,
                        displayedItems = sortedItems
                    )
                } else {
                    loadItems()
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error updating item: ${e.message}", e)
                loadItems()
            }
        }
    }
    
    /**
     * Delete a grocery item
     */
    fun deleteGroceryItem(groceryItem: GroceryItem) {
        viewModelScope.launch {
            try {
                repository.deleteGroceryItem(groceryItem)
                
                // Remove from local state
                val currentItems = _uiState.value.allItems.toMutableList()
                val removed = currentItems.removeIf { it.id == groceryItem.id }
                
                if (removed) {
                    val filteredItems = applyFilters(currentItems)
                    val sortedItems = sortItems(filteredItems)
                    
                    _uiState.value = _uiState.value.copy(
                        allItems = currentItems,
                        displayedItems = sortedItems
                    )
                } else {
                    loadItems()
                }
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error deleting item: ${e.message}", e)
                loadItems()
            }
        }
    }
    
    /**
     * Clear all checked items
     */
    fun clearCheckedItems() {
        viewModelScope.launch {
            try {
                repository.deleteAllCheckedItems(getCurrentUserId())
                
                // Remove checked items from local state
                val currentItems = _uiState.value.allItems.toMutableList()
                val filtered = currentItems.filter { !it.isChecked }
                
                val filteredForDisplay = applyFilters(filtered)
                val sortedItems = sortItems(filteredForDisplay)
                
                _uiState.value = _uiState.value.copy(
                    allItems = filtered,
                    displayedItems = sortedItems
                )
            } catch (e: Exception) {
                Log.e("GroceryViewModel", "Error clearing checked items: ${e.message}", e)
                loadItems()
            }
        }
    }
    
    override fun onLogout() {
        _searchQuery.value = ""
        _showCheckedItems.value = true
        _categoryFilter.value = null
        _uiState.value = GroceryUiState()
    }
}