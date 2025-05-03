package com.cs501.pantrypal.screen.profilePage

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.firebase.FirebaseService
import com.cs501.pantrypal.screen.profilePage.components.EditOptionsCard
import com.cs501.pantrypal.screen.profilePage.components.IngredientEditDialog
import com.cs501.pantrypal.screen.profilePage.components.MyPantryCard
import com.cs501.pantrypal.screen.profilePage.components.ProfileTopBar
import com.cs501.pantrypal.screen.profilePage.components.UserInfoSection
import com.cs501.pantrypal.screen.profilePage.components.photoPicker
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@SuppressLint("SimpleDateFormat")
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    userIngredientsViewModel: UserIngredientsViewModel,
    recipesViewModel: RecipeViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val ingredients by userIngredientsViewModel.allIngredients.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var showEditOptions by remember { mutableStateOf(false) }
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    val imgLauncher = photoPicker("profile", "name", userViewModel, snackbarHostState)
    val clickedIngredient = remember { mutableStateOf<UserIngredients?>(null) }

    if (showAddIngredientDialog) {
        IngredientEditDialog(
            userIngredientsViewModel = userIngredientsViewModel, onDismiss = {
                showAddIngredientDialog = false
                clickedIngredient.value = null
            }, snackbarHostState = snackbarHostState, ingredient = clickedIngredient.value
        )
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .background(color = Color.Black.copy(alpha = 0.4f))
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = InfoColor
            )
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (currentUser == null) {
                return NotLoggedInContent { navController.navigate("login") }
            }

            ProfileTopBar {
                userViewModel.logout()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Logout successful",
                    )
                }
                navController.navigate("discover")
            }

            Spacer(modifier = Modifier.height(16.dp))

            UserInfoSection(
                user = currentUser!!,
                onEditClick = { showEditOptions = !showEditOptions },
                onImageClick = { imgLauncher.launch("image/*") }) {
                coroutineScope.launch {
                    isLoading = true
                    val syncResult =
                        handleSync(userViewModel, userIngredientsViewModel, recipesViewModel)

                    isLoading = false
                    snackbarHostState.showSnackbar("${syncResult["message"]}")
                }
            }

            if (showEditOptions) {
                EditOptionsCard(ingredients, userViewModel, recipesViewModel, snackbarHostState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            MyPantryCard(
                ingredients = ingredients,
                onAddIngredient = { showAddIngredientDialog = true },
                onIngredientClick = { ingredient ->
                    showAddIngredientDialog = true
                    clickedIngredient.value = ingredient
                })
        }
    }
}

@Composable
fun NotLoggedInContent(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_image),
                contentDescription = "Default Profile",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You are not logged in", style = Typography.titleMedium
            )

            Text(
                text = "Please login to view your profile", style = Typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Login")
            }
        }
    }
}

suspend fun handleSync(
    userViewModel: UserViewModel,
    userIngredientsViewModel: UserIngredientsViewModel,
    recipesViewModel: RecipeViewModel,
): Map<String, Any> {
    val firebaseService = FirebaseService.getInstance()
    val user = userViewModel.currentUser.value!!
    val ingredientsRestored = firebaseService.restoreUserIngredients(user.id)
    val recipeRestored = firebaseService.restoreRecipes(user.id)
    var syncResult = ""

    syncResult += if (ingredientsRestored != emptyList<UserIngredients>()) {
        userIngredientsViewModel.updateAllIngredients(ingredientsRestored)
        "User ingredients synced successfully.\r\n"
    } else {
        "No ingredients to sync.\r\n"
    }

    syncResult += if (recipeRestored != emptyList<SavedRecipe>()) {
        recipesViewModel.updateAllRecipes(recipeRestored, user.id)
        "Recipes synced successfully.\r\n"
    } else {
        "No recipes to sync.\r\n"
    }

    if (ingredientsRestored != emptyList<UserIngredients>() && recipeRestored != emptyList<SavedRecipe>()) {
        return mapOf("success" to true, "message" to syncResult)
    }

    return mapOf("success" to false, "message" to syncResult)
}