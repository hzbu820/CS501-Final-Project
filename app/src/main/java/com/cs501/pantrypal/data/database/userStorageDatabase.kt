package com.cs501.pantrypal.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_ingredients")
data class UserIngredients(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var quantity: String = "",
    var unit: String = "",
    var expirationDate: String = "",
    var location: String = "",
    var notes: String = "",
    var isFavorite: Boolean = false
)

@Dao
interface UserIngredientsDao {
    @Query("SELECT * FROM user_ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<UserIngredients>>
    
    @Query("SELECT * FROM user_ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Int): UserIngredients?

    @Query("SELECT * FROM user_ingredients WHERE name = :name")
    suspend fun getIngredientByName(name: String): UserIngredients?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: UserIngredients): Long
    
    @Update
    suspend fun updateIngredient(ingredient: UserIngredients)
    
    @Delete
    suspend fun deleteIngredient(ingredient: UserIngredients)
    
    @Query("SELECT * FROM user_ingredients WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchIngredients(searchQuery: String): Flow<List<UserIngredients>>
    
    @Query("SELECT * FROM user_ingredients WHERE isFavorite = 1")
    fun getFavoriteIngredients(): Flow<List<UserIngredients>>
}

