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

    private val _ingredients = MutableStateFlow<List<UserIngredients>>(emptyList())
    val ingredients: StateFlow<List<UserIngredients>> = _ingredients.asStateFlow()

    private val _favoriteIngredients = MutableStateFlow<List<UserIngredients>>(emptyList())
    val favoriteIngredients: StateFlow<List<UserIngredients>> = _favoriteIngredients.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserIngredientsRepository(database.userIngredientsDao())

        viewModelScope.launch {
            repository.allIngredients.collect { ingredients ->
                _ingredients.value = ingredients
            }
        }

        viewModelScope.launch {
            repository.favoriteIngredients.collect { favorites ->
                _favoriteIngredients.value = favorites
            }
        }
    }

    override fun onUserIdChanged(userId: Int) {
    }

    fun addIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: UserIngredients) {
        viewModelScope.launch {
            repository.deleteIngredient(ingredient)
        }
    }

    fun toggleFavorite(ingredient: UserIngredients) {
        viewModelScope.launch {
            val updatedIngredient = ingredient.copy(isFavorite = !ingredient.isFavorite)
            repository.updateIngredient(updatedIngredient)
        }
    }

    fun searchIngredients(query: String) {
        viewModelScope.launch {
            repository.searchIngredients(query).collect { results ->
                _ingredients.value = results
            }
        }
    }
}