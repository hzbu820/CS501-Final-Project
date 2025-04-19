package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.Typography
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
            Text("Recipes in $cookbookName", style = Typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))

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
                                Text(recipe.label, style = MaterialTheme.typography.titleMedium)
                                Text(recipe.ingredientLines.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(onClick = {
                                viewModel.deleteSavedRecipe(recipe)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Recipe",
                                    tint = ErrorColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
