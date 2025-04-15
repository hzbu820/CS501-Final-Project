package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.PrimaryLight
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeSearchScreen(viewModel: RecipeViewModel, navController: NavController, snackbarHostState: SnackbarHostState) {

    var ingredients by remember { mutableStateOf(listOf("")) }
    val maxIngredients = 5

    var showShakeInfo by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Discover Recipes", style = Typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Enter ingredients (max $maxIngredients)", 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        IngredientInputSection(
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            onIngredientChange = { index, value -> 
                ingredients = ingredients.toMutableList().apply {
                    this[index] = value
                }
            },
            onAddIngredient = {
                if (ingredients.size < maxIngredients) {
                    ingredients = ingredients + ""
                }
            },
            onRemoveIngredient = { index ->
                if (ingredients.size > 1) {
                    ingredients = ingredients.filterIndexed { i, _ -> i != index }
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    val nonEmptyIngredients = ingredients.filter { it.isNotBlank() }
                    if (nonEmptyIngredients.isNotEmpty()) {
                        viewModel.searchRecipes(nonEmptyIngredients.joinToString(", "))
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(InfoColor,)
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box {
                Button(onClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message = "Function Not Yet Implemented", duration = SnackbarDuration.Long)
                    }

                }) {
                    Text("Shake for Surprise", style = MaterialTheme.typography.labelLarge)
                }
                
                IconButton(
                    onClick = { showShakeInfo = true },
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Information",
                        tint = InfoColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(viewModel.recipes) { recipe ->
                    val isSaved = viewModel.isRecipeSaved(recipe)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                viewModel.selectedRecipe = recipe
                                navController.navigate("detail")
                                //navController.navigate("detail/${Uri.encode(recipe.uri)}")
                            }
                    ) {
                        Column {
                            Text(recipe.label, style = MaterialTheme.typography.titleMedium)
                            AsyncImage(model = recipe.image, contentDescription = null)
                            Row {
                                TextButton(onClick = {
                                    if (isSaved) {
                                        viewModel.deleteRecipeByUrl(recipe.uri ?: "")
                                    } else {
                                        viewModel.saveRecipeToCookbook(recipe)
                                    }
                                }) {
                                    Text(if (isSaved) "Remove" else "Save", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showShakeInfo) {
            AlertDialog(
                onDismissRequest = { showShakeInfo = false },
                title = { Text("Shake for Surprise") },
                text = {
                    Text("Shake your device to get a random recipe suggestion based on trending ingredients! Perfect when you're not sure what to cook.")
                },
                confirmButton = {
                    TextButton(onClick = { showShakeInfo = false }) {
                        Text("Got it!")
                    }
                }
            )
        }
    }
}

@Composable
fun IngredientInputSection(
    ingredients: List<String>,
    maxIngredients: Int,
    onIngredientChange: (Int, String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ingredients.forEachIndexed { index, ingredient ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                TextField(
                    value = ingredient,
                    onValueChange = { onIngredientChange(index, it) },
                    label = { Text("Ingredient ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                IconButton(onClick = { onRemoveIngredient(index) }, enabled = ingredients.size > 1) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Remove Ingredient",
                        tint = if (ingredients.size > 1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }
        
        if (ingredients.size < maxIngredients) {
            TextButton(
                onClick = onAddIngredient,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = InfoColor),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Ingredient")
            }
        }
    }
}

