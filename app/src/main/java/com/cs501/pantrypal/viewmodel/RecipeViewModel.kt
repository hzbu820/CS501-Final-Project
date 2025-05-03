package com.cs501.pantrypal.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.data.repository.RecipeRepository
import com.cs501.pantrypal.data.repository.UserIngredientsRepository
import com.cs501.pantrypal.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : BaseViewModel(application) {
    private val repository: RecipeRepository
    private val userRepository: UserRepository
    private val userIngredientsRepository: UserIngredientsRepository

    var recipes by mutableStateOf<List<Recipe>>(emptyList())
    var isLoading by mutableStateOf(false)
    var selectedRecipe: Recipe? by mutableStateOf(null)

    private val _savedRecipes = MutableStateFlow<List<SavedRecipe>>(emptyList())
    val savedRecipes: StateFlow<List<SavedRecipe>> = _savedRecipes.asStateFlow()

    private val _cookbooks = MutableStateFlow<List<String>>(emptyList())
    val cookbooks: StateFlow<List<String>> = _cookbooks

    private val _cookbookRecipeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cookbookRecipeCounts: StateFlow<Map<String, Int>> = _cookbookRecipeCounts

    // Fallback trending ingredients list if user's pantry is empty
    private val trendingIngredients = listOf(
        "chicken", "pasta", "beef", "rice", "salmon", 
        "tofu", "broccoli", "kale", "avocado", "quinoa",
        "shrimp", "tomato", "potato", "mushroom", "spinach"
    )

    // User's pantry ingredients
    private val _pantryIngredients = MutableStateFlow<List<UserIngredients>>(emptyList())
    val pantryIngredients: StateFlow<List<UserIngredients>> = _pantryIngredients.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RecipeRepository(database.savedRecipeDao())
        userRepository = UserRepository(database.userDao())
        userIngredientsRepository = UserIngredientsRepository(database.userIngredientsDao())
        
        viewModelScope.launch {
            loadSavedRecipes()
            loadCookbooks()
            loadPantryIngredients()
        }
    }

    override fun onUserIdChanged(userId: String) {
        loadSavedRecipes()
        loadPantryIngredients()
    }
    
    /**
     * Load ingredients from the user's pantry
     */
    private fun loadPantryIngredients() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == "") {
                Log.w("PantryPal", "No user logged in, cannot load pantry ingredients")
                return@launch
            }
            try {
                userIngredientsRepository.getAllIngredientsByUserId(userId).collect { ingredients ->
                    _pantryIngredients.value = ingredients
                }
            } catch (e: Exception) {
                Log.e("PantryPal", "Error loading pantry ingredients: ${e.message}", e)
            }
        }
    }
    
    /**
     * Reload current user's saved recipes from the database
     */
    private fun loadSavedRecipes() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == "") {
                Log.w("PantryPal", "No user logged in, cannot load saved recipes")
                return@launch
            }
            try {
                repository.getAllRecipesByUserId(getCurrentUserId()).collect { recipes ->
                    _savedRecipes.value = recipes
                }
            } catch (e: Exception) {
                Log.e("PantryPal", "Error loading saved recipes: ${e.message}", e)
            }
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
     * Get surprise recipes using ingredients from the user's pantry
     * This is triggered by the shake gesture
     */
    fun getRandomRecipes() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Get ingredients from the user's pantry
                val pantryIngredients = _pantryIngredients.value
                
                // If pantry is empty, use trending ingredients as fallback
                if (pantryIngredients.isEmpty()) {
                    Log.d("PantryPal", "Pantry is empty, using trending ingredients instead")
                    // Select 1-3 random ingredients from the trending list
                    val numberOfIngredients = (1..3).random()
                    val randomIngredients = trendingIngredients.shuffled().take(numberOfIngredients)
                    
                    // Join the ingredients and search
                    val query = randomIngredients.joinToString(", ")
                    Log.d("PantryPal", "Shake activated! Searching for trending ingredients: $query")
                    
                    recipes = repository.searchRecipesFromApi(query)
                    Log.d("PantryPal", "Fetched ${recipes.size} random recipes")
                } else {
                    // Select 1-3 random ingredients from the user's pantry
                    val numberOfIngredients = minOf(3, pantryIngredients.size).coerceAtLeast(1)
                    val randomPantryIngredients = pantryIngredients.shuffled().take(numberOfIngredients)
                    
                    // Extract ingredient names
                    val ingredientNames = randomPantryIngredients.map { it.name }
                    val query = ingredientNames.joinToString(", ")
                    
                    Log.d("PantryPal", "Shake activated! Searching for pantry ingredients: $query")
                    
                    recipes = repository.searchRecipesFromApi(query)
                    Log.d("PantryPal", "Fetched ${recipes.size} recipes using pantry ingredients")
                }
            } catch (e: Exception) {
                Log.e("PantryPal", "Error getting random recipes: ${e.message}", e)
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
        selectedRecipe =recipe.toRecipe()
    }



    fun loadCookbooks() {
        viewModelScope.launch {
            repository.getAllRecipesByUserId(getCurrentUserId()).collect { recipes ->
                _savedRecipes.value = recipes
                _cookbooks.value = recipes.map { it.cookbookName }.distinct()
                _cookbookRecipeCounts.value = recipes.groupingBy { it.cookbookName }.eachCount()
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

    suspend fun isRecipeInCookbook(url: String, cookbookName: String): Boolean {
        return repository.isRecipeInCookbook(url, cookbookName)
    }

    fun createCookbook(name: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (userId == "") {
                Log.w("PantryPal", "Cannot create cookbook: No user logged in")
                return@launch
            }

            // placeholder
            //TODO: Actually input label image and url
            val placeholder = SavedRecipe(
                label = "placeholder recipe",
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


    val savedRecipeUrls = derivedStateOf {
        savedRecipes.value.map { it.url }.toSet()
    }

    fun isRecipeSaved(recipe: Recipe): Boolean {
        return savedRecipeUrls.value.contains(recipe.uri ?: "")
    }

    fun saveRecipeToCookbook(recipe: Recipe, cookbookName: String = "Default", isFavorite: Boolean = false) {
        if (!isUserLoggedIn()) {
            Log.w("PantryPal", "Cannot save recipe: No user logged in")
            return
        }

        viewModelScope.launch {

            val savedRecipe = recipe.toSavedRecipe(getCurrentUserId(), isFavorite, cookbookName)
            repository.insertRecipe(savedRecipe)
            repository.deletePlaceholderRecipesFromCookbook(cookbookName)
            loadRecipesByCookbook(cookbookName)
            //loadSavedRecipes()
        }
    }

    fun deleteRecipeByUrl(url: String) {
        viewModelScope.launch {
            val matching = savedRecipes.value.firstOrNull { it.url == url }
            matching?.let {
                repository.deleteRecipe(it)
                loadSavedRecipes()
            }
        }
    }

//    var selectedApiRecipe by mutableStateOf<Recipe?>(null)
//
//    fun selectApiRecipe(recipe: Recipe) {
//        selectedApiRecipe = recipe
//    }

    fun Recipe.toSavedRecipe(
        userId: String,
        isFavorite: Boolean = false,
        cookbookName: String = "default"
    ): SavedRecipe {
        return SavedRecipe(
            label = this.label,
            image = this.image,
            url = this.uri,
            ingredientLines = this.ingredientLines,
            calories = this.calories,
            isFavorite = isFavorite,
            userId = userId,
            cookbookName = cookbookName
        )
    }

    fun SavedRecipe.toRecipe(): Recipe {
        return Recipe(
            label = this.label,
            image = this.image,
            ingredientLines = this.ingredientLines,
            uri = this.url,
            calories = this.calories,
            yield = 0.0,
            totalTime = 0.0,
            cuisineType = listOf()
        )
    }

    fun updateAllRecipes(recipes: List<SavedRecipe>, userId: String) {
        viewModelScope.launch {
            repository.deleteAllRecipesByUserId(userId)
            repository.updateAllRecipes(recipes)
        }
    }

    fun getAllSavedRecipes(): List<SavedRecipe> {
        return _savedRecipes.value
    }

}
