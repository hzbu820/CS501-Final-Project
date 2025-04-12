package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.database.SavedRecipeDao
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.data.network.ApiClient
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val savedRecipeDao: SavedRecipeDao) {
    val allRecipes: Flow<List<SavedRecipe>> = savedRecipeDao.getAllRecipes()
    val favoriteRecipes: Flow<List<SavedRecipe>> = savedRecipeDao.getFavoriteRecipes()

    suspend fun getRecipeById(id: Int): SavedRecipe? {
        return savedRecipeDao.getRecipeById(id)
    }

    suspend fun insertRecipe(recipe: SavedRecipe): Long {
        return savedRecipeDao.insertRecipe(recipe)
    }

    suspend fun updateRecipe(recipe: SavedRecipe) {
        savedRecipeDao.updateRecipe(recipe)
    }

    suspend fun deleteRecipe(recipe: SavedRecipe) {
        savedRecipeDao.deleteRecipe(recipe)
    }

    fun searchRecipes(query: String): Flow<List<SavedRecipe>> {
        return savedRecipeDao.searchRecipes(query)
    }

    suspend fun searchRecipesFromApi(query: String): List<Recipe> {
        val response = ApiClient.retrofit.searchRecipes(ingredients = query)
        return response.hits.map { it.recipe }
    }

    fun displayFavoriteRecipes(): Flow<List<SavedRecipe>> {
        return favoriteRecipes
    }

    //TODO: Can add functions to format recipe data here
}