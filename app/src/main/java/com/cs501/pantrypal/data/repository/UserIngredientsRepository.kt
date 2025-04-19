package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.database.UserIngredientsDao
import kotlinx.coroutines.flow.Flow

class UserIngredientsRepository(private val userIngredientsDao: UserIngredientsDao) {
    /**
     * Get all ingredients by user ID
     */
    fun getAllIngredientsByUserId(userId: Int): Flow<List<UserIngredients>> {
        return userIngredientsDao.getAllIngredientsByUserId(userId)
    }

    /**
     * Get all favorite ingredients by user ID
     */
    fun getFavoriteIngredientsByUserId(userId: Int): Flow<List<UserIngredients>> {
        return userIngredientsDao.getFavoriteIngredientsByUserId(userId)
    }

    /**
     * Search ingredients by user ID
     */
    fun searchIngredientsByUserId(searchQuery: String, userId: Int): Flow<List<UserIngredients>> {
        return userIngredientsDao.searchIngredientsByUserId(searchQuery, userId)
    }

    /**
     * Search ingredients by user ID and food category
     */
    fun searchIngredientsByUserIdAndName(category: String, userId: Int): Flow<List<UserIngredients>> {
        return userIngredientsDao.searchIngredientsByUserIdAndCategory(category, userId)
    }

    /**
     * Search ingredients by user ID and expiration date
     */
    fun searchIngredientsByUserIdAndExpirationDate(expirationDate: String, userId: Int): Flow<List<UserIngredients>> {
        return userIngredientsDao.searchIngredientsByUserIdAndExpirationDate(expirationDate, userId)
    }

    /**
     * Insert a new ingredient to the database
     */
    suspend fun insertIngredient(ingredient: UserIngredients): Long {
        return userIngredientsDao.insertIngredient(ingredient)
    }

    /**
     * Update an ingredient in the database
     */
    suspend fun updateIngredient(ingredient: UserIngredients) {
        userIngredientsDao.updateIngredient(ingredient)
    }

    /**
     * Delete an ingredient from the database
     */
    suspend fun deleteIngredient(ingredient: UserIngredients) {
        userIngredientsDao.deleteIngredient(ingredient)
    }

}