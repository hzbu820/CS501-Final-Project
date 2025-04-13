package com.cs501.pantrypal.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.data.repository.RecipeRepository
import com.cs501.pantrypal.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : BaseViewModel(application) {
    private val repository: RecipeRepository
    var recipes by mutableStateOf<List<Recipe>>(emptyList())
    var isLoading by mutableStateOf(false)
    var selectedRecipe: Recipe? by mutableStateOf(null)

    private val _savedRecipes = MutableStateFlow<List<SavedRecipe>>(emptyList())
    val savedRecipes: StateFlow<List<SavedRecipe>> = _savedRecipes.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RecipeRepository(database.savedRecipeDao())
        userRepository = UserRepository(database.userDao())
    }

    override fun onUserIdChanged(userId: Int) {
        loadSavedRecipes()
    }
    
    /**
     * Reload current user's saved recipes from the database
     */
    private fun loadSavedRecipes() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId <= 0) {
                Log.w("PantryPal", "No user logged in, cannot load saved recipes")
                return@launch
            }
            repository.getAllRecipesByUserId(getCurrentUserId())
        }
    }
    
    fun searchRecipes(query: String) {
        Log.d("PantryPal", "Start searching for $query")
        viewModelScope.launch {
            isLoading = true
            try {
                recipes = repository.searchRecipesFromApi(query)
                Log.d("PantryPal", "Fetched ${recipes.size} recipes")
            } catch (e: Exception) {
                Log.e("PantryPal", "Error: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }
}
