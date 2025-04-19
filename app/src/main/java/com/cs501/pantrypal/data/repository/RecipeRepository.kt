package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.database.SavedRecipeDao
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.data.network.ApiClient
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val savedRecipeDao: SavedRecipeDao) {
    val allRecipes: Flow<List<SavedRecipe>> = savedRecipeDao.getAllRecipes()

    /**
     * Get all saved recipes by user ID
     */
    fun getAllRecipesByUserId(userId: Int): Flow<List<SavedRecipe>> {
        return savedRecipeDao.getRecipesByUserId(userId)
    }
    
    /**
     * Get all favorite recipes by user ID
     */
    fun getFavoriteRecipesByUser(userId: Int): Flow<List<SavedRecipe>> {
        return savedRecipeDao.getFavoriteRecipesByUser(userId)
    }

    /**
     * Insert a new recipe to the database
     */
    suspend fun insertRecipe(recipe: SavedRecipe): Long {
        return savedRecipeDao.insertRecipe(recipe)
    }

    /**
     * Remove a recipe from the database
     */
    suspend fun deleteRecipe(recipe: SavedRecipe) {
        savedRecipeDao.deleteRecipe(recipe)
    }

    suspend fun deleteRecipesByCookbook(cookbook: String) {
        savedRecipeDao.deleteByCookbook(cookbook)
    }


    /**
     * Search user's saved recipes
     */
    fun searchRecipesByUser(query: String, userId: Int): Flow<List<SavedRecipe>> {
        return savedRecipeDao.searchRecipesByUser(query, userId)
    }
    
    /**
     * Get a recipe from the Api
     */
    suspend fun searchRecipesFromApi(query: String): List<Recipe> {
        val response = ApiClient.retrofit.searchRecipes(ingredients = query)
        return response.hits.map { it.recipe }
    }

    /**
     * Get recipes by cookbook name
     */
    fun getRecipesByCookbook(cookbookName: String): Flow<List<SavedRecipe>> {
        return savedRecipeDao.getRecipesByCookbook(cookbookName)
    }

    fun getAllCookbookNames(): Flow<List<String>> {
        return savedRecipeDao.getAllCookbookNames()
    }

}