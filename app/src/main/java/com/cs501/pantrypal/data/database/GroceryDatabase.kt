package com.cs501.pantrypal.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "grocery_items")
data class GroceryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: String,
    val unit: String,
    val category: String,
    val isChecked: Boolean = false,
    val userId: String,// Set to "" as global items
    val dateAdded: Long = System.currentTimeMillis()
)

@Dao
interface GroceryItemDao {
    @Query("SELECT * FROM grocery_items WHERE userId = :userId ORDER BY isChecked ASC, dateAdded DESC")
    fun getAllGroceryItemsByUserId(userId: String): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE userId = :userId AND isChecked = :isChecked ORDER BY dateAdded DESC")
    fun getGroceryItemsByCheckedStatus(userId: String, isChecked: Boolean): Flow<List<GroceryItem>>

    @Query("SELECT * FROM grocery_items WHERE userId = :userId AND name LIKE '%' || :searchQuery || '%' ORDER BY isChecked ASC, dateAdded DESC")
    fun searchGroceryItems(userId: String, searchQuery: String): Flow<List<GroceryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(groceryItem: GroceryItem): Long

    @Update
    suspend fun updateGroceryItem(groceryItem: GroceryItem)

    @Delete
    suspend fun deleteGroceryItem(groceryItem: GroceryItem)

    @Query("DELETE FROM grocery_items WHERE userId = :userId AND isChecked = 1")
    suspend fun deleteAllCheckedItems(userId: String)

    @Query("SELECT * FROM grocery_items WHERE userId = :userId AND category = :category AND isChecked = :isChecked ORDER BY dateAdded DESC")
    fun getGroceryItemsByCategory(userId: String, category: String, isChecked: Boolean): Flow<List<GroceryItem>>
} 