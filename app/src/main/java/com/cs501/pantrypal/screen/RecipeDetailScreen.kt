
package com.cs501.pantrypal.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.data.model.Recipe
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.cs501.pantrypal.ui.theme.InfoColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(viewModel: RecipeViewModel, navController: NavController) {
    val recipe = viewModel.selectedRecipe ?: return
    val cookbooks by viewModel.cookbooks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val isTablet = screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.label, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                            viewModel.saveRecipeToCookbook(recipe, cookbookName)
                            snackbarHostState.showSnackbar("Added to $cookbookName!")
                        }
                    }
                }
            )
        }
    ) { padding ->
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
        },
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (recipe.yield > 0) {
        Text("Yield: ${recipe.yield} servings", style = MaterialTheme.typography.bodyLarge)
    }
    if (recipe.totalTime > 0) {
        Text("Total Time: ${recipe.totalTime} minutes", style = MaterialTheme.typography.bodyLarge)
    }
    if (recipe.cuisineType.isNotEmpty()) {
        Text("Cuisine: ${recipe.cuisineType.joinToString()}", style = MaterialTheme.typography.bodyLarge)
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
            text = { Text("Add to Cookbook") }
        )
        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onDropdownChange(false) }
        ) {
            allCookbooks.forEach { cookbookName ->
                DropdownMenuItem(
                    text = { Text(cookbookName) },
                    onClick = {
                        onDropdownChange(false)
                        onAdd(cookbookName)
                    }
                )
            }
        }
    }
}

//fun RecipeDetailScreen(viewModel: RecipeViewModel, navController: NavController) {
//    val recipe = viewModel.selectedRecipe ?: return
//    val cookbooks by viewModel.cookbooks.collectAsState()
//    val savedRecipes by viewModel.savedRecipes.collectAsState()
//    val snackbarHostState = remember { SnackbarHostState() }
//    val coroutineScope = rememberCoroutineScope()
//    var isDropdownExpanded by remember { mutableStateOf(false) }
//
//    val isSaved = remember(savedRecipes) {
//        savedRecipes.any { it.url == recipe.uri }
//    }
//
//    val currentSavedRecipe = remember(savedRecipes) {
//        savedRecipes.firstOrNull { it.url == recipe.uri }
//    }
//    val formattedCalories = String.format("%.2f", recipe.calories)
//
//    LaunchedEffect(Unit) {
//        viewModel.loadCookbooks()
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(recipe.label, maxLines = 1) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Back"
//                        )
//                    }
//                }
//            )
//        },
//        snackbarHost = {
//            SnackbarHost(hostState = snackbarHostState)
//        },
//        floatingActionButton = {
//
//            Column {
//                ExtendedFloatingActionButton(
//                    onClick = {
//                        isDropdownExpanded = true
//                    },
//                    icon = {
//                        Icon(
//                            imageVector = Icons.Default.Favorite,
//                            contentDescription = "Favorite"
//                        )
//                    },
//                    text = {
//                        Text("Add to Cookbook")
//                    }
//                )
//
//                // 合并已有 cookbooks 和默认 COOKBOOK_CATEGORIES
//                val allCookbookOptions = remember(cookbooks) {
//                    (cookbooks + Constants.COOKBOOK_CATEGORIES).toSet().toList().sorted()
//                }
//
//                DropdownMenu(
//                    expanded = isDropdownExpanded,
//                    onDismissRequest = { isDropdownExpanded = false }
//                ) {
//                    allCookbookOptions.forEach { cookbookName ->
//                        DropdownMenuItem(
//                            text = { Text(cookbookName) },
//                            onClick = {
//                                isDropdownExpanded = false
//                                coroutineScope.launch {
//                                    val exists = viewModel.isRecipeInCookbook(recipe.uri, cookbookName)
//                                    if (exists) {
//                                            snackbarHostState.showSnackbar("Recipe already exists in \"$cookbookName\"!")
//                                       // isDropdownExpanded = false
//                                    } else {
//                                        viewModel.saveRecipeToCookbook(recipe, cookbookName)
//                                        snackbarHostState.showSnackbar("Added to $cookbookName!")
//
//                                      //  isDropdownExpanded = false
//                                    }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(padding)
//                .padding(16.dp)
//        ) {
//            item {
//                AsyncImage(
//                    model = recipe.image,
//                    contentDescription = null,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text("Description", fontWeight = FontWeight.Bold)
//                Text(
//                    text = buildAnnotatedString {
//                        withStyle(style = SpanStyle(color = Color(0xFF388E3C))) { // ✅ 绿色数字
//                            append(formattedCalories)
//                        }
//                        append(" ")
//                        withStyle(style = SpanStyle(color = Color.Black)) { // ✅ 黑色单位
//                            append("calories")
//                        }
//                    },
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 8.dp)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                if (recipe.yield > 0) {
//                    Text("Yield: ${recipe.yield} servings", style = MaterialTheme.typography.bodyLarge)
//                }
//
//                if (recipe.totalTime > 0) {
//                    Text("Total Time: ${recipe.totalTime} minutes", style = MaterialTheme.typography.bodyLarge)
//                }
//
//                if (recipe.cuisineType.isNotEmpty()) {
//                    Text(
//                        text = "Cuisine: ${recipe.cuisineType.joinToString()}",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                //Text(recipe.uri)
//                Text("Ingredients:",  fontWeight = FontWeight.Bold)
//            }
//
//            items(recipe.ingredientLines) { line ->
//                Text("\u2022 $line")
//            }
//        }
//    }
//}
