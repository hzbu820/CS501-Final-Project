package com.cs501.pantrypal.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.AppViewModelProvider
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(navController: NavController) {
    val viewModel: RecipeViewModel = AppViewModelProvider.recipeViewModel
    val recipe = viewModel.selectedRecipe ?: return
    val cookbooks by viewModel.cookbooks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(recipe.label, maxLines = 1) }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        },

        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = InfoColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },

        floatingActionButton = {
            AddToCookbookButton(
                recipe = recipe,
                cookbooks = cookbooks,
                isDropdownExpanded = isDropdownExpanded,
                onDropdownChange = { isDropdownExpanded = it },
                onAdd = { cookbookName ->
                    coroutineScope.launch {
                        val exists = viewModel.isRecipeInCookbook(recipe.uri, cookbookName)
                        if (exists) {
                            snackbarHostState.showSnackbar("Already exists in \"$cookbookName\"!")
                        } else {
                            val success = viewModel.saveRecipeToCookbook(recipe, cookbookName)
                            if (!success) {
                                snackbarHostState.showSnackbar("You have not logged in yet!")
                            }else{
                                snackbarHostState.showSnackbar("Added to $cookbookName!")
                            }
                        }
                    }
                })
        }) { padding ->
        if (isTablet) {
            TabletRecipeDetailContent(recipe = recipe, padding = padding)
        } else {
            PhoneRecipeDetailContent(recipe = recipe, padding = padding)
        }
    }
}


@Composable
fun PhoneRecipeDetailContent(recipe: Recipe, padding: PaddingValues) {
    val formattedCalories = String.format("%.2f", recipe.calories)
    LazyColumn(
        modifier = Modifier
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
            RecipeDescription(recipe, formattedCalories)
            Text("Ingredients:", fontWeight = FontWeight.Bold)
        }
        items(recipe.ingredientLines) { line ->
            Text("• $line")
        }
    }
}


@Composable
fun TabletRecipeDetailContent(recipe: Recipe, padding: PaddingValues) {
    val formattedCalories = String.format("%.2f", recipe.calories)
    Row(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = recipe.image,
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        Spacer(modifier = Modifier.width(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                RecipeDescription(recipe, formattedCalories)
                Text("Ingredients:", fontWeight = FontWeight.Bold)
            }
            items(recipe.ingredientLines) { line ->
                Text("• $line")
            }
        }
    }
}


@Composable
fun RecipeDescription(recipe: Recipe, formattedCalories: String) {
    Text("Description", fontWeight = FontWeight.Bold)
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFF388E3C))) {
                append(formattedCalories)
            }
            append(" ")
            withStyle(SpanStyle(color = Color.Black)) {
                append("calories")
            }
        }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)
    )

    if (recipe.yield > 0) {
        Text("Yield: ${recipe.yield} servings", style = MaterialTheme.typography.bodyLarge)
    }
    if (recipe.totalTime > 0) {
        Text("Total Time: ${recipe.totalTime} minutes", style = MaterialTheme.typography.bodyLarge)
    }
    if (recipe.cuisineType.isNotEmpty()) {
        Text(
            "Cuisine: ${recipe.cuisineType.joinToString()}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}


@Composable
fun AddToCookbookButton(
    recipe: Recipe,
    cookbooks: List<String>,
    isDropdownExpanded: Boolean,
    onDropdownChange: (Boolean) -> Unit,
    onAdd: (String) -> Unit
) {
    val allCookbooks = remember(cookbooks) {
        (cookbooks + Constants.COOKBOOK_CATEGORIES).toSet().toList().sorted()
    }

    Column {
        ExtendedFloatingActionButton(
            onClick = { onDropdownChange(true) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorite") },
            text = { Text("Add to Cookbook") })
        DropdownMenu(
            expanded = isDropdownExpanded, onDismissRequest = { onDropdownChange(false) }) {
            allCookbooks.forEach { cookbookName ->
                DropdownMenuItem(text = { Text(cookbookName) }, onClick = {
                    onDropdownChange(false)
                    onAdd(cookbookName)
                })
            }
        }
    }
}
