package com.cs501.pantrypal

import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs501.pantrypal.navigation.BottomNavigationBar
import com.cs501.pantrypal.screen.CookBookDetailScreen
import com.cs501.pantrypal.screen.CookBookScreen
import com.cs501.pantrypal.screen.GroceryListScreen
import com.cs501.pantrypal.screen.RecipeDetailScreen
import com.cs501.pantrypal.screen.RecipeSearchScreen
import com.cs501.pantrypal.screen.profilePage.LoginScreen
import com.cs501.pantrypal.screen.profilePage.ProfileScreen
import com.cs501.pantrypal.screen.profilePage.RegisterScreen
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.PantryPalTheme
import com.cs501.pantrypal.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = if (resources.configuration.screenWidthDp >= 600) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
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
    AppViewModelProvider.initialize(application)
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(bottomBar = { BottomNavigationBar(navController) }, snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                snackbarData = data, containerColor = InfoColor
            )
        }
    }) { paddingValues ->
        NavHost(
            navController, startDestination = "discover", modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(navController, snackbarHostState)
            }
            composable("register") {
                RegisterScreen(navController, snackbarHostState)
            }
            composable("discover") {
                RecipeSearchScreen(navController, snackbarHostState)
            }
            composable("cookbook") {
                CookBookScreen(navController)
            }
            composable("groceryList") {
                GroceryListScreen(snackbarHostState)
            }
            composable("profile") {
                ProfileScreen(navController, snackbarHostState)
            }
            composable("detail") { backStack ->
                AppViewModelProvider.recipeViewModel.selectedRecipe?.let {
                    RecipeDetailScreen(navController)
                } ?: Text("No recipe selected", style = Typography.displayMedium)
            }
            composable(
                route = "cookbook_detail/{cookbookName}",
                arguments = listOf(navArgument("cookbookName") { type = NavType.StringType })
            ) { backStackEntry ->
                val cookbookName = backStackEntry.arguments?.getString("cookbookName") ?: "default"
                CookBookDetailScreen(
                    cookbookName,
                    navController,
                    AppViewModelProvider.recipeViewModel
                )
            }
            composable("recipe_detail") {
                RecipeDetailScreen(navController)
            }

        }
    }
}
