package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    var ingredients by remember { mutableStateOf(listOf("")) }
    val maxIngredients = 5
    var showShakeInfo by remember { mutableStateOf(false) }

    if (isTablet) {
        TabletRecipeSearchLayout(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState,
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            showShakeInfo = showShakeInfo,
            onShowShakeInfoChange = { showShakeInfo = it },
            onIngredientsChange = { ingredients = it }
        )
    } else {
        PhoneRecipeSearchLayout(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState,
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            showShakeInfo = showShakeInfo,
            onShowShakeInfoChange = { showShakeInfo = it },
            onIngredientsChange = { ingredients = it }
        )
    }

//    if (showShakeInfo) {
//        AlertDialog(
//            onDismissRequest = { onShowShakeInfoChange(false) },
//            title = { Text("Shake for Surprise") },
//            text = {
//                Text("Shake your device to get a random recipe suggestion based on trending ingredients! Perfect when you're not sure what to cook.")
//            },
//            confirmButton = {
//                TextButton(onClick = { onShowShakeInfoChange(false) }) {
//                    Text("Got it!")
//                }
//            }
//        )
//    }
}

@Composable
fun TabletRecipeSearchLayout(
    viewModel: RecipeViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    ingredients: List<String>,
    maxIngredients: Int,
    showShakeInfo: Boolean,
    onShowShakeInfoChange: (Boolean) -> Unit,
    onIngredientsChange: (List<String>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Row(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ){
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text("Discover", style = Typography.displayLarge)
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
                        onIngredientsChange(ingredients.toMutableList().apply {
                            this[index] = value
                        })
                    },
                    onAddIngredient = {
                        if (ingredients.size < maxIngredients) {
                            onIngredientsChange(ingredients + "")
                        }
                    },
                    onRemoveIngredient = { index ->
                        if (ingredients.size > 1) {
                            onIngredientsChange(ingredients.filterIndexed { i, _ -> i != index })
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
                        colors = ButtonDefaults.buttonColors(InfoColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Search")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

//                Box {
//                    Button(
//                        onClick = {
//                            coroutineScope.launch {
//                                snackbarHostState.showSnackbar(message = "Function Not Yet Implemented", duration = SnackbarDuration.Long)
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Text("Shake for Surprise", style = MaterialTheme.typography.labelLarge)
//                    }
//
//                    IconButton(
//                        onClick = { onShowShakeInfoChange(true) },
//                        modifier = Modifier
//                            .size(24.dp)
//                            .align(Alignment.TopEnd)
//                            .offset(x = 8.dp, y = (-8).dp)
//                    ) {
//                        Icon(
//                            Icons.Default.Info,
//                            contentDescription = "Information",
//                            tint = InfoColor,
//                            modifier = Modifier.size(16.dp)
//                        )
//                    }
//                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f)
                .padding(16.dp)
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.recipes) { recipe ->
                        val isSaved = viewModel.isRecipeSaved(recipe)
                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedRecipe = recipe
                                    navController.navigate("detail")
                                }
                        ) {
                            Column {
                                Text(
                                    recipe.label, 
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                                AsyncImage(
                                    model = recipe.image, 
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(modifier = Modifier.padding(8.dp)) {
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
        }
    }
}

@Composable
fun PhoneRecipeSearchLayout(
    viewModel: RecipeViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    ingredients: List<String>,
    maxIngredients: Int,
    showShakeInfo: Boolean,
    onShowShakeInfoChange: (Boolean) -> Unit,
    onIngredientsChange: (List<String>) -> Unit
) {
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
                onIngredientsChange(ingredients.toMutableList().apply {
                    this[index] = value
                })
            },
            onAddIngredient = {
                if (ingredients.size < maxIngredients) {
                    onIngredientsChange(ingredients + "")
                }
            },
            onRemoveIngredient = { index ->
                if (ingredients.size > 1) {
                    onIngredientsChange(ingredients.filterIndexed { i, _ -> i != index })
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

//            Box {
//                Button(onClick = {
//                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar(message = "Function Not Yet Implemented", duration = SnackbarDuration.Long)
//                    }
//
//                }) {
//                    Text("Shake for Surprise", style = MaterialTheme.typography.labelLarge)
//                }
//
//                IconButton(
//                    onClick = { onShowShakeInfoChange(true) },
//                    modifier = Modifier
//                        .size(24.dp)
//                        .align(Alignment.TopEnd)
//                        .offset(x = 8.dp, y = (-8).dp)
//                ) {
//                    Icon(
//                        Icons.Default.Info,
//                        contentDescription = "Information",
//                        tint = InfoColor,
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
//            }
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
                OutlinedTextField(
                    value = ingredient,
                    onValueChange = { onIngredientChange(index, it) },
                    label = { Text("Ingredient ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (ingredients.size > 1) {
                            IconButton(onClick = { onRemoveIngredient(index) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove Ingredient")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    )
                )

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

