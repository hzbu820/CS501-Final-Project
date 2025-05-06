package com.cs501.pantrypal

import android.app.Application
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import com.cs501.pantrypal.viewmodel.GroceryViewModel

/**
 * Global ViewModel
 */
object AppViewModelProvider {
    private lateinit var application: Application

    val recipeViewModel by lazy { RecipeViewModel(application) }
    val userViewModel by lazy { UserViewModel(application) }
    val userIngredientsViewModel by lazy { UserIngredientsViewModel(application) }
    val groceryViewModel by lazy { GroceryViewModel(application) }

    fun initialize(app: Application) {
        if (!::application.isInitialized) {
            application = app
        }
    }
}