package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.GroceryItem
import com.cs501.pantrypal.data.database.GroceryItemDao
import kotlinx.coroutines.flow.Flow

class GroceryRepository(private val groceryItemDao: GroceryItemDao) {
    /**
     * Get all grocery items by user ID
     */
    fun getAllGroceryItemsByUserId(userId: Int): Flow<List<GroceryItem>> {
        return groceryItemDao.getAllGroceryItemsByUserId(userId)
    }

    /**
     * Get grocery items by checked status
     */
    fun getGroceryItemsByCheckedStatus(userId: Int, isChecked: Boolean): Flow<List<GroceryItem>> {
        return groceryItemDao.getGroceryItemsByCheckedStatus(userId, isChecked)
    }
    
    /**
     * Search grocery items
     */
    fun searchGroceryItems(userId: Int, searchQuery: String): Flow<List<GroceryItem>> {
        return groceryItemDao.searchGroceryItems(userId, searchQuery)
    }
    
    /**
     * Insert a new grocery item
     */
    suspend fun insertGroceryItem(groceryItem: GroceryItem): Long {
        return groceryItemDao.insertGroceryItem(groceryItem)
    }
    
    /**
     * Update a grocery item
     */
    suspend fun updateGroceryItem(groceryItem: GroceryItem) {
        groceryItemDao.updateGroceryItem(groceryItem)
    }
    
    /**
     * Delete a grocery item
     */
    suspend fun deleteGroceryItem(groceryItem: GroceryItem) {
        groceryItemDao.deleteGroceryItem(groceryItem)
    }
    
    /**
     * Delete all checked items
     */
    suspend fun deleteAllCheckedItems(userId: Int) {
        groceryItemDao.deleteAllCheckedItems(userId)
    }

    /**
     * Get grocery items by category
     */
    fun getGroceryItemsByCategory(userId: Int, category: String, isChecked: Boolean): Flow<List<GroceryItem>> {
        return groceryItemDao.getGroceryItemsByCategory(userId, category, isChecked)
    }

} 