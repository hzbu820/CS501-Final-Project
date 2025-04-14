package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.database.UserIngredientsDao
import kotlinx.coroutines.flow.Flow

class UserIngredientsRepository(private val userIngredientsDao: UserIngredientsDao) {
    val allIngredients: Flow<List<UserIngredients>> = userIngredientsDao.getAllIngredients()
    val favoriteIngredients: Flow<List<UserIngredients>> = userIngredientsDao.getFavoriteIngredients()

    suspend fun getIngredientById(id: Int): UserIngredients? {
        return userIngredientsDao.getIngredientById(id)
    }

    suspend fun getIngredientByName(name: String): UserIngredients? {
        return userIngredientsDao.getIngredientByName(name)
    }

    suspend fun insertIngredient(ingredient: UserIngredients): Long {
        return userIngredientsDao.insertIngredient(ingredient)
    }

    suspend fun updateIngredient(ingredient: UserIngredients) {
        userIngredientsDao.updateIngredient(ingredient)
    }

    suspend fun deleteIngredient(ingredient: UserIngredients) {
        userIngredientsDao.deleteIngredient(ingredient)
    }

    fun searchIngredients(query: String): Flow<List<UserIngredients>> {
        return userIngredientsDao.searchIngredients(query)
    }
}