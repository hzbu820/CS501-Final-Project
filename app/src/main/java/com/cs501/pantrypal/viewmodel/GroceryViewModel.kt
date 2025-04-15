package com.cs501.pantrypal.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.GroceryItem
import com.cs501.pantrypal.data.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Grocery ViewModel
 */
class GroceryViewModel(application: Application) : BaseViewModel(application) {
    private val repository: GroceryRepository

    // StateFlow for all grocery items
    private val _allGroceryItems = MutableStateFlow<List<GroceryItem>>(emptyList())
    val allGroceryItems: StateFlow<List<GroceryItem>> = _allGroceryItems.asStateFlow()

    // StateFlow for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow for showing checked items
    private val _showCheckedItems = MutableStateFlow(true)
    val showCheckedItems: StateFlow<Boolean> = _showCheckedItems.asStateFlow()

    // StateFlow for category filter
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GroceryRepository(database.groceryItemDao())

        getAllGroceryItems()
    }

    override fun onUserIdChanged(userId: Int) {
        getAllGroceryItems()
    }

    fun getAllGroceryItems() {
        viewModelScope.launch {
            repository.getAllGroceryItemsByUserId(getCurrentUserId()).collect { items ->
                _allGroceryItems.value = items
            }
        }
    }

    fun searchGroceryItems(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            getAllGroceryItems()
        } else {
            viewModelScope.launch {
                repository.searchGroceryItems(getCurrentUserId(), query).collect { items ->
                    _allGroceryItems.value = items
                }
            }
        }
    }

    fun toggleShowCheckedItems() {
        _showCheckedItems.value = !_showCheckedItems.value
        refreshGroceryItems()
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
        refreshGroceryItems()
    }

    private fun refreshGroceryItems() {
        viewModelScope.launch {
            // Apply filters based on checked status and category
            val userId = getCurrentUserId()
            val showChecked = _showCheckedItems.value
            val category = _categoryFilter.value
            
            when {
                // Filter by both checked status and category
                !showChecked && category != null -> {
                    repository.getGroceryItemsByCategory(userId, category, false ).collect { items ->
                        _allGroceryItems.value = items
                    }
                }
                // Filter by checked status only
                !showChecked -> {
                    repository.getGroceryItemsByCheckedStatus(userId, false).collect { items ->
                        _allGroceryItems.value = items
                    }
                }
                // Filter by category only
                category != null -> {
                    repository.getGroceryItemsByCategory(userId, category, false).collect { items ->
                        _allGroceryItems.value = items
                    }
                }
                // No filters
                else -> {
                    getAllGroceryItems()
                }
            }
        }
    }

    fun addGroceryItem(name: String, quantity: String, unit: String, category: String) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            val groceryItem = GroceryItem(
                name = name,
                quantity = quantity,
                unit = unit,
                category = category,
                userId = getCurrentUserId()
            )
            repository.insertGroceryItem(groceryItem)
            getAllGroceryItems()
        }
    }

    fun updateGroceryItem(groceryItem: GroceryItem) {
        viewModelScope.launch {
            repository.updateGroceryItem(groceryItem)
            getAllGroceryItems()
        }
    }

    fun toggleItemChecked(groceryItem: GroceryItem) {
        viewModelScope.launch {
            val updatedItem = groceryItem.copy(isChecked = !groceryItem.isChecked)
            repository.updateGroceryItem(updatedItem)
            getAllGroceryItems()
        }
    }

    fun deleteGroceryItem(groceryItem: GroceryItem) {
        viewModelScope.launch {
            repository.deleteGroceryItem(groceryItem)
            getAllGroceryItems()
        }
    }

    fun clearCheckedItems() {
        viewModelScope.launch {
            repository.deleteAllCheckedItems(getCurrentUserId())
            getAllGroceryItems()
        }
    }
}