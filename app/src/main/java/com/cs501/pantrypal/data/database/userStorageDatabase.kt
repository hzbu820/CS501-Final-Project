package com.cs501.pantrypal.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_ingredients")
data class UserIngredients(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var foodCategory: String = "",
    var image: String = "",
    var quantity: String = "",
    var unit: String = "",
    var expirationDate: String = "",
    var location: String = "",
    var notes: String = "",
    var isFavorite: Boolean = false,
    var userId: String
)

@Dao
interface UserIngredientsDao {
    @Query("SELECT * FROM user_ingredients WHERE userId = :userId ORDER BY name ASC")
    fun getAllIngredientsByUserId(userId: String): Flow<List<UserIngredients>>

    @Query("SELECT * FROM user_ingredients WHERE userId = :userId AND isFavorite=1 ORDER BY name ASC")
    fun getFavoriteIngredientsByUserId(userId: String): Flow<List<UserIngredients>>

    @Query("SELECT * FROM user_ingredients WHERE userId = :userId AND name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchIngredientsByUserId(searchQuery: String, userId: String): Flow<List<UserIngredients>>

    @Query("SELECT * FROM user_ingredients WHERE userId = :userId AND name LIKE '%' || '%' AND foodCategory = :foodCategory ORDER BY name ASC")
    fun searchIngredientsByUserIdAndCategory(foodCategory: String, userId: String): Flow<List<UserIngredients>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: UserIngredients): Long

    @Update
    suspend fun updateIngredient(ingredient: UserIngredients)

    @Delete
    suspend fun deleteIngredient(ingredient: UserIngredients)

    @Query("SELECT * FROM user_ingredients WHERE userId = :userId AND name LIKE '%' || '%' AND expirationDate = :expirationDate ORDER BY name ASC")
    fun searchIngredientsByUserIdAndExpirationDate(expirationDate: String, userId: String): Flow<List<UserIngredients>>
}

