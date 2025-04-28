package com.cs501.pantrypal.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Ingredient(
    val text: String,
    val weight: Double,
    val foodCategory: String,
    val foodId: String,
    val image: String
)

class IngredientListConverter {
    @TypeConverter
    fun fromIngredientList(value: List<Ingredient>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toIngredientList(value: String): List<Ingredient> {
        val gson = Gson()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, type)
    }
}

@Entity(tableName = "saved_recipes")
data class SavedRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String,
    val image: String,
    val url: String,
    val ingredientLines: List<String>,
    val calories: Double,
    val isFavorite: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis(),
    val userId: String = "",
    val cookbookName: String = "default"
)

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(separator = "|||")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return value.split("|||")
    }
}

@Dao
interface SavedRecipeDao {
    @Query("SELECT * FROM saved_recipes ORDER BY dateAdded DESC")
    fun getAllRecipes(): Flow<List<SavedRecipe>>
    
    @Query("SELECT * FROM saved_recipes WHERE userId = :userId ORDER BY dateAdded DESC")
    fun getRecipesByUserId(userId: String): Flow<List<SavedRecipe>>
    
    @Query("SELECT * FROM saved_recipes WHERE id = :id")
    suspend fun getRecipeById(id: Int): SavedRecipe?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: SavedRecipe): Long
    
    @Update
    suspend fun updateRecipe(recipe: SavedRecipe)
    
    @Delete
    suspend fun deleteRecipe(recipe: SavedRecipe)

    @Query("SELECT * FROM saved_recipes WHERE label LIKE '%' || :searchQuery || '%'")
    fun searchRecipes(searchQuery: String): Flow<List<SavedRecipe>>
    
    @Query("SELECT * FROM saved_recipes WHERE label LIKE '%' || :searchQuery || '%' AND userId = :userId")
    fun searchRecipesByUser(searchQuery: String, userId: Int): Flow<List<SavedRecipe>>
    
    @Query("SELECT * FROM saved_recipes WHERE isFavorite = 1")
    fun getFavoriteRecipes(): Flow<List<SavedRecipe>>
    
    @Query("SELECT * FROM saved_recipes WHERE isFavorite = 1 AND userId = :userId")
    fun getFavoriteRecipesByUser(userId: String): Flow<List<SavedRecipe>>

    @Query("SELECT * FROM saved_recipes WHERE cookbookName = :cookbookName")
    fun getRecipesByCookbook(cookbookName: String): Flow<List<SavedRecipe>>

    @Query("SELECT DISTINCT cookbookName FROM saved_recipes")
    fun getAllCookbookNames(): Flow<List<String>>

    @Query("DELETE FROM saved_recipes WHERE cookbookName = :cookbook")
    suspend fun deleteByCookbook(cookbook: String)

    @Query("SELECT * FROM saved_recipes WHERE url = :url AND cookbookName = :cookbookName LIMIT 1")
    suspend fun isRecipeInCookbook(url: String, cookbookName: String): SavedRecipe?


}
