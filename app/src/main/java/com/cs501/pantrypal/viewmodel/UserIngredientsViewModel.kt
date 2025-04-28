package com.cs501.pantrypal.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.repository.UserIngredientsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * User's ingredients ViewModel
 */
class UserIngredientsViewModel(application: Application) : BaseViewModel(application) {
    private val repository: UserIngredientsRepository

    // StateFlow for current all of the user's ingredients
    private val _allIngredients = MutableStateFlow<List<UserIngredients>>(emptyList())
    val allIngredients: StateFlow<List<UserIngredients>> = _allIngredients.asStateFlow()

    // StateFlow for current user's searched ingredients
    private val _ingredients = MutableStateFlow<List<UserIngredients>>(emptyList())
    val ingredients: StateFlow<List<UserIngredients>> = _ingredients.asStateFlow()

    // StateFlow for current user's searched ingredients by barcode
    private val _ingredientsByBarcode = MutableStateFlow<UserIngredients>(
        UserIngredients(
            id = 0, userId = "", name = "", unit = "", expirationDate = "", isFavorite = false
        )
    )
    val ingredientsByBarcode: StateFlow<UserIngredients> = _ingredientsByBarcode.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserIngredientsRepository(database.userIngredientsDao())

        getAllIngredients()
    }

    override fun onUserIdChanged(userId: String) {
        getAllIngredients()
    }

    fun getAllIngredients() {
        viewModelScope.launch {
            repository.getAllIngredientsByUserId(getCurrentUserId()).collect { ingredients ->
                _allIngredients.value = ingredients
            }
        }
    }

    fun addIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
            getAllIngredients()
        }
    }

    fun updateIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.updateIngredient(ingredient)
            getAllIngredients()
        }
    }

    fun deleteIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
            getAllIngredients()
        }
    }

    fun searchIngredients(query: String) {
        viewModelScope.launch {
            repository.searchIngredientsByUserId(query, getCurrentUserId()).collect { results ->
                _ingredients.value = results
            }
        }
    }

    fun setEmptyBarcodeIngredient() {
        _ingredientsByBarcode.value = UserIngredients(userId = "")
    }

    fun searchIngredientsByApi(query: String) {
        viewModelScope.launch {
            setEmptyBarcodeIngredient()
            val foodResponse = repository.searchIngredientsByBarcode(query)


            _ingredientsByBarcode.value = UserIngredients(
                userId = getCurrentUserId(),
                name = foodResponse.food.food_name,
                image = foodResponse.food_images?.food_image[0]?.image_url ?: "",
            )

        }
    }

    fun updateAllIngredients(ingredients: List<UserIngredients>) {
//        viewModelScope.launch {
//            repository.updateAllIngredients(ingredients)
//            getAllIngredients()
//        }
    }
}