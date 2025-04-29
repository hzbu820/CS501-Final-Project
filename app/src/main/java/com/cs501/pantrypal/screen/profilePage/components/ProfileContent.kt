package com.cs501.pantrypal.screen.profilePage.components

import android.net.Uri
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel

@Composable
fun ProfileContent(
    user: User,
    profileImageUri: Uri?,
    showEditOptions: Boolean,
    ingredients: List<UserIngredients>,
    onToggleEditOptions: () -> Unit,
    onSelectImage: () -> Unit,
    onNavigateToAddIngredient: () -> Unit,
    onSyncClick: () -> Unit = {},
    navController: NavController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    snackbarHostState: SnackbarHostState
) {
    // User Info Section
    UserInfoSection(user, profileImageUri, onToggleEditOptions, onSelectImage, onSyncClick)

    // Edit User Info Section
    if (showEditOptions) {
        EditOptionsCard(user, ingredients, navController, userViewModel, recipeViewModel, snackbarHostState )
    }

    // User Pantry Section
    MyPantrySection(ingredients, onNavigateToAddIngredient)
}