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
    private val userRepository: UserRepository

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
    var isLoading by mutableStateOf(false)
    var selectedRecipe: Recipe? by mutableStateOf(null)

    private val _savedRecipes = MutableStateFlow<List<SavedRecipe>>(emptyList())
    val savedRecipes: StateFlow<List<SavedRecipe>> = _savedRecipes.asStateFlow()

    private val _cookbooks = MutableStateFlow<List<String>>(emptyList())
    val cookbooks: StateFlow<List<String>> = _cookbooks

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
    
    /**
     * Search for recipes from the API
     */
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
    
    /**
     * Save a recipe to the database
     */
    fun saveRecipe(recipe: Recipe, isFavorite: Boolean = false) {
        if (!isUserLoggedIn()) {
            Log.w("PantryPal", "Cannot save recipe: No user logged in")
            return
        }
        
        viewModelScope.launch {
            try {
                // Transform the Recipe object to SavedRecipe
                val savedRecipe = SavedRecipe(
                    label = recipe.label,
                    image = recipe.image,
                    url = recipe.uri ?: "",
                    ingredientLines = recipe.ingredientLines,
                    calories = 0.0,
                    isFavorite = isFavorite,
                    userId = getCurrentUserId()
                )
                repository.insertRecipe(savedRecipe)
                Log.d("PantryPal", "Recipe saved: ${recipe.label}")

                loadSavedRecipes()
            } catch (e: Exception) {
                Log.e("PantryPal", "Error saving recipe: ${e.message}", e)
            }
        }
    }

    fun deleteSavedRecipe(recipe: SavedRecipe) {
        viewModelScope.launch {
            repository.deleteRecipe(recipe)
            loadRecipesByCookbook(recipe.cookbookName)
        }
    }

    fun selectSavedRecipe(recipe: SavedRecipe) {
        //selectedRecipe = recipe
    }


    fun loadCookbooks() {
        viewModelScope.launch {
            repository.getAllCookbookNames().collect {
                _cookbooks.value = it
            }
        }
    }

    fun loadRecipesByCookbook(cookbook: String) {
        viewModelScope.launch {
            repository.getRecipesByCookbook(cookbook).collect {
                _savedRecipes.value = it
            }
        }
    }

    fun createCookbook(name: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId <= 0) {
                Log.w("PantryPal", "Cannot create cookbook: No user logged in")
                return@launch
            }

            // 新建一个空的 SavedRecipe 作为 cookbook 占位（或根据需求定义结构）
            val placeholder = SavedRecipe(
                label = "",  // 空标题，表示只是一个容器
                image = "",
                url = "",
                ingredientLines = listOf(),
                calories = 0.0,
                isFavorite = false,
                userId = userId,
                cookbookName = name
            )

            repository.insertRecipe(placeholder)
            loadCookbooks()
        }
    }

    fun getRecipeCountInCookbook(cookbook: String): Int {
        return _savedRecipes.value.count { it.cookbookName == cookbook }
    }

    fun deleteCookbook(cookbook: String) {
        viewModelScope.launch {
            repository.deleteRecipesByCookbook(cookbook)
            loadCookbooks()
        }
    }



}
