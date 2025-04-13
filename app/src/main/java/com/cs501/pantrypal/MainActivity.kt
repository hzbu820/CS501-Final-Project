package com.cs501.pantrypal

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cs501.pantrypal.ui.theme.PantryPalTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.cs501.pantrypal.navigation.BottomNavigationBar
import com.cs501.pantrypal.screen.*
import com.cs501.pantrypal.screen.profilePage.LoginScreen
import com.cs501.pantrypal.screen.profilePage.ProfileScreen
import com.cs501.pantrypal.screen.profilePage.RegisterScreen
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserProfileViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PantryPalTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {
    val application = LocalContext.current.applicationContext as Application
    val recipeViewModel = remember { RecipeViewModel(application) }
    val userProfileViewModel = remember { UserProfileViewModel(application) }
    val userViewModel = remember { UserViewModel(application) }
    val userIngredientsViewModel = remember { UserIngredientsViewModel(application) }
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController,
            startDestination = "discover",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(userViewModel, navController, snackbarHostState)
            }
            composable("register") {
                RegisterScreen(userViewModel, navController, snackbarHostState)
            }
            composable("discover") {
                RecipeSearchScreen(recipeViewModel, navController)
            }
            composable("cookbook") {
                CookBookScreen()
            }
            composable("grocerylist") {
                GroceryListScreen()
            }
            composable("profile") {
                ProfileScreen(userViewModel, navController, snackbarHostState, userIngredientsViewModel)
            }
            composable("detail") { backStack ->
                recipeViewModel.selectedRecipe?.let {
                    RecipeDetailScreen(it, navController)
                } ?: Text("No recipe selected")
            }
        }
    }
}
