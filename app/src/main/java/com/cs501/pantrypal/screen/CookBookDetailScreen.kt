package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookBookDetailScreen(
    cookbookName: String,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val recipes by viewModel.savedRecipes.collectAsState()

    LaunchedEffect(cookbookName) {
        viewModel.loadRecipesByCookbook(cookbookName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = cookbookName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Recipes in $cookbookName", fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes.filter { it.cookbookName == cookbookName }) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.selectSavedRecipe(recipe)
                                        navController.navigate("recipe_detail")
                                    }
                            ) {
                                Text(recipe.label, fontSize = 16.sp)
                                Text(recipe.ingredientLines.joinToString(", "), fontSize = 12.sp)
                            }

                            IconButton(onClick = {
                                viewModel.deleteSavedRecipe(recipe)
                            }) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_delete),
                                    contentDescription = "Delete Recipe"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
