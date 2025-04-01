package com.cs501.pantrypal.viewmodel


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.data.network.ApiClient
import kotlinx.coroutines.launch



class RecipeViewModel : ViewModel() {
    var recipes by mutableStateOf<List<Recipe>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
    var selectedRecipe: Recipe? by mutableStateOf(null)


    fun searchRecipes(query: String) {
        Log.d("PantryPal", "Start searching for $query")
        viewModelScope.launch {
            isLoading = true
            try {
                val response = ApiClient.retrofit.searchRecipes(
                    type = "public",
                    ingredients = query,
                    appId = "e5544b43",
                    appKey = "c1449006ec0d6009a0f969caaf890c60"
                )
                Log.d("PantryPal", "Fetched ${response.hits.size} recipes")
                recipes = response.hits.map { it.recipe }
            } catch (e: Exception) {
                // TODO: error handling
                Log.e("PantryPal", "Error: ${e.message}", e)
            }finally {
                isLoading = false   // ✅ 必须加
            }

        }
    }
}
