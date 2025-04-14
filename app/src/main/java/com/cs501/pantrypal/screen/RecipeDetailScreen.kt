package com.cs501.pantrypal.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(viewModel: RecipeViewModel, navController: NavController) {
    val recipe = viewModel.selectedRecipe ?: return
    val cookbooks by viewModel.cookbooks.collectAsState()
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCookbook by remember { mutableStateOf("") }

    val isSaved = remember(savedRecipes) {
        savedRecipes.any { it.url == recipe.uri }
    }

    val currentSavedRecipe = remember(savedRecipes) {
        savedRecipes.firstOrNull { it.url == recipe.uri }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCookbooks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.label, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            Column {
                if (!isSaved) {
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCookbook,
                            onValueChange = { selectedCookbook = it },
                            label = { Text("Choose Cookbook") },
                            modifier = Modifier.menuAnchor(),
                            readOnly = true
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            cookbooks.forEach { cookbook ->
                                DropdownMenuItem(
                                    text = { Text(cookbook) },
                                    onClick = {
                                        selectedCookbook = cookbook
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ExtendedFloatingActionButton(
                    onClick = {
                        if (isSaved && currentSavedRecipe != null) {
                            viewModel.deleteSavedRecipe(currentSavedRecipe)
                        } else {
                            val cookbookToUse = selectedCookbook.ifBlank { "default" }
                            viewModel.saveRecipeToCookbook(recipe, cookbookToUse)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Added to cookbook!")
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite"
                        )
                    },
                    text = {
                        Text(if (isSaved) "Remove from Cookbook" else "Add to Cookbook")
                    }
                )
            }
        }


    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
        ) {
            item {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Description", fontWeight = FontWeight.Bold)
                Text("Ingredients:")
            }

            items(recipe.ingredientLines) { line ->
                Text("• $line")
            }
        }
    }
}

