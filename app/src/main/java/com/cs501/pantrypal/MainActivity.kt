package com.cs501.pantrypal

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cs501.pantrypal.ui.theme.PantryPalTheme
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.navigation.BottomNavigationBar
import com.cs501.pantrypal.screen.*
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import java.net.URLDecoder

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
    val viewModel = remember { RecipeViewModel() }
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController,
            startDestination = "discover",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("discover") {
                RecipeSearchScreen(viewModel, navController)
            }
            composable("cookbook") {
                CookBookScreen()
            }
            composable("grocerylist") {
                GroceryListScreen()
            }
            composable("profile") {
                ProfileScreen()
            }

            composable("detail") { backStack ->
                viewModel.selectedRecipe?.let {
                    RecipeDetailScreen(it, navController)
                } ?: Text("No recipe selected")
            }
        }
    }
}
