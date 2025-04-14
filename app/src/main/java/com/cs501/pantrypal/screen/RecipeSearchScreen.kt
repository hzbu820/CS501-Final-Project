package com.cs501.pantrypal.screen


import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.data.model.Recipe

@Composable
fun RecipeSearchScreen(viewModel: RecipeViewModel, navController: NavController) {
    var input by remember { mutableStateOf("") }


    Column(modifier = Modifier.padding(16.dp)) {
        Text("Enter up to 5 ingredients", fontSize = 20.sp)
        TextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("e.g., chicken, garlic") },
            modifier = Modifier.fillMaxWidth()
        )

        Row {
//            Button(onClick = { viewModel.searchRecipes(input) }) {
//                Text("Search")
//            }
            Button(onClick = {
                if (input.isNotBlank()) {
                    viewModel.searchRecipes(input)
                }
            }) {
                Text("Search")
            }

            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { /* TODO: Shake Surprise */ }) {
                Text("Shake for Surprise")
            }
        }

        if (viewModel.isLoading) {
            CircularProgressIndicator()
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
                            Text(recipe.label, fontWeight = FontWeight.Bold)
                            AsyncImage(model = recipe.image, contentDescription = null)
                            Row {
                                TextButton(onClick = {
                                    if (isSaved) {
                                        viewModel.deleteRecipeByUrl(recipe.uri ?: "")
                                    } else {
                                        viewModel.saveRecipeToCookbook(recipe)
                                    }
                                }) {
                                    Text(if (isSaved) "Remove" else "Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

