package com.cs501.pantrypal.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.AppViewModelProvider
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.TextPrimaryLight
import com.cs501.pantrypal.ui.theme.TextSecondaryLight
import com.cs501.pantrypal.ui.theme.TextTertiary
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.Constants.COOKBOOK_CATEGORIES
import com.cs501.pantrypal.viewmodel.RecipeViewModel

@Composable
fun CookBookScreen(
    navController: NavController
) {
    val viewModel: RecipeViewModel = AppViewModelProvider.recipeViewModel
    val cookbooks by viewModel.cookbooks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newCookbookName by remember { mutableStateOf("") }
    val recipeCounts by viewModel.cookbookRecipeCounts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCookbook by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.loadCookbooks()
        isLoading = false
    }

    // Load recipes when a cookbook is selected
    LaunchedEffect(selectedCookbook) {
        selectedCookbook?.let { viewModel.loadRecipesByCookbook(it) }
    }

    // Check if device is a tablet based on screen width
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Show loading indicator if data is being loaded
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Loading...", style = Typography.bodyLarge, color = TextPrimaryLight
            )
        }
        return
    } else {
        if (isTablet) {
            // Tablet layout with sidebar
            TabletCookBookLayout(
                cookbooks = cookbooks,
                recipeCounts = recipeCounts,
                searchQuery = searchQuery,
                selectedCookbook = selectedCookbook,
                navController = navController,
                viewModel = viewModel,
                onSearchQueryChange = { searchQuery = it },
                onClearSearch = { searchQuery = "" },
                onCookbookSelect = { selectedCookbook = it },
                onDeleteCookbook = { viewModel.deleteCookbook(it) },
                onShowAddDialog = { showDialog = true })
        } else {
            // Phone layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                CookBookScreenHeader()

                Box(modifier = Modifier.fillMaxSize()) {
                    if (cookbooks.isEmpty()) {
                        EmptyCookBookState()
                    } else {
                        CookBookList(cookbooks, recipeCounts, navController, viewModel)
                    }

                    // Floating Action Button to add a new cookbook
                    AddCookBookButton(Modifier.align(Alignment.BottomEnd)) { showDialog = true }
                }
            }
        }
    }
    // Add Cookbook Dialog
    if (showDialog) {
        AddCookBookDialog(
            newCookbookName = newCookbookName,
            onNameChange = { newCookbookName = it },
            onDismiss = {
                newCookbookName = ""
                showDialog = false
            },
            onConfirm = {
                if (newCookbookName.isNotBlank()) {
                    viewModel.createCookbook(newCookbookName.trim())
                    newCookbookName = ""
                    showDialog = false
                }
            })
    }
}

@Composable
fun CookBookScreenHeader() {
    Text(
        text = "My Cook Books",
        style = Typography.displayLarge,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

@Composable
fun EmptyCookBookState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your cookbook is empty",
            style = Typography.displayMedium,
            color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Add a new cookbook to get started",
            style = Typography.bodyMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CookBookList(
    cookbooks: List<String>,
    recipeCounts: Map<String, Int>,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cookbooks) { cookbookName ->
            CookBookItem(
                cookbookName = cookbookName,
                recipeCount = recipeCounts[cookbookName] ?: 0,
                onItemClick = {
                    viewModel.loadRecipesByCookbook(cookbookName)
                    navController.navigate("cookbook_detail/${cookbookName}")
                },
                onDeleteClick = { viewModel.deleteCookbook(cookbookName) })
        }
    }
}

@Composable
fun CookBookItem(
    cookbookName: String, recipeCount: Int, onItemClick: () -> Unit, onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onItemClick)
            ) {
                Text(
                    text = cookbookName, style = Typography.titleLarge
                )
                Text(
                    text = "$recipeCount recipes",
                    style = Typography.bodySmall,
                    color = TextTertiary,
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Cookbook",
                    tint = ErrorColor
                )
            }
        }
    }
}

@Composable
fun AddCookBookButton(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier.padding(16.dp),
        containerColor = InfoColor,
        shape = CircleShape,
    ) {
        Icon(
            imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = Color.White
        )
    }
}

@Composable
fun TabletCookBookLayout(
    cookbooks: List<String>,
    recipeCounts: Map<String, Int>,
    searchQuery: String,
    selectedCookbook: String?,
    navController: NavController,
    viewModel: RecipeViewModel,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCookbookSelect: (String?) -> Unit,
    onDeleteCookbook: (String) -> Unit,
    onShowAddDialog: () -> Unit
) {
    var isCustomCookbooksExpanded by remember { mutableStateOf(true) }
    val defaultCategories = COOKBOOK_CATEGORIES
    val customCookbooks = cookbooks.filterNot { defaultCategories.contains(it) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left sidebar with cookbooks and search
        Card(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(12.dp)
            ) {
                // Header
                Text(
                    text = "My Cook Books",
                    style = Typography.displayMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Search cookbooks") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search, contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Cookbooks header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Customized Cookcooks",
                        style = Typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    TextButton(
                        onClick = { isCustomCookbooksExpanded = !isCustomCookbooksExpanded },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                    ) {
                        if (isCustomCookbooksExpanded) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Collapse",
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Expand",
                            )
                        }
                    }
                }

                // Custom Cookbooks list (expandable)
                if (isCustomCookbooksExpanded) {
                    val filteredCookbooks = if (searchQuery.isEmpty()) {
                        customCookbooks
                    } else {
                        customCookbooks.filter { it.contains(searchQuery, ignoreCase = true) }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCookbooks) { cookbookName ->
                            CookbookSidebarItem(
                                cookbookName = cookbookName,
                                recipeCount = recipeCounts[cookbookName] ?: 0,
                                isSelected = selectedCookbook == cookbookName,
                                onCookbookSelect = {
                                    onCookbookSelect(cookbookName)
                                },
                                onDeleteClick = { onDeleteCookbook(cookbookName) })
                        }
                    }

                }

                // Default Categories header
                Text(
                    text = "Default Categories",
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Default cookbook categories from Constants
                LazyColumn(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultCategories) { category ->
                        CookbookSidebarItem(
                            cookbookName = category,
                            recipeCount = recipeCounts[category] ?: 0,
                            isSelected = selectedCookbook == category,
                            onCookbookSelect = {
                                onCookbookSelect(category)
                            })
                    }
                }

                // Add cookbook button
                Button(
                    onClick = onShowAddDialog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Cookbook",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add Cookbook")
                }
            }
        }

        // Right content area with cookbook details
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Cookbook title if selected
            if (selectedCookbook != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                ) {
                    IconButton(onClick = { onCookbookSelect(null) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, contentDescription = "Back"
                        )
                    }
                    Text(
                        text = selectedCookbook,
                        style = Typography.displayMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                RecipeListForCookbook(
                    cookbookName = selectedCookbook,
                    navController = navController,
                    viewModel = viewModel
                )
            } else {
                // Show all cookbooks in card format
                Text(
                    text = "All Cookbooks",
                    style = Typography.displayMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                )

                if (cookbooks.isEmpty()) {
                    EmptyCookBookState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val filteredCookbooks = if (searchQuery.isEmpty()) {
                            cookbooks
                        } else {
                            cookbooks.filter { it.contains(searchQuery, ignoreCase = true) }
                        }

                        items(filteredCookbooks) { cookbookName ->
                            CookBookItem(
                                cookbookName = cookbookName,
                                recipeCount = recipeCounts[cookbookName] ?: 0,
                                onItemClick = { onCookbookSelect(cookbookName) },
                                onDeleteClick = { onDeleteCookbook(cookbookName) })
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun CookbookSidebarItem(
    cookbookName: String,
    recipeCount: Int,
    isSelected: Boolean,
    onCookbookSelect: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onCookbookSelect() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cookbookName, style = MaterialTheme.typography.bodyLarge, color = textColor
            )
            Text(
                text = "$recipeCount recipes",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) textColor.copy(alpha = 0.7f) else TextTertiary
            )
        }

        if (onDeleteClick != null && cookbookName != "All Cookbooks") {
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Cookbook",
                    tint = ErrorColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddCookBookDialog(
    newCookbookName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New Cookbook") }, text = {
        OutlinedTextField(
            value = newCookbookName,
            onValueChange = onNameChange,
            label = { Text("Cookbook name") })
    }, confirmButton = {
        TextButton(onClick = onConfirm) {
            Text("Create")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

@Composable
fun RecipeListForCookbook(
    cookbookName: String?, navController: NavController, viewModel: RecipeViewModel
) {
    val savedRecipes by viewModel.savedRecipes.collectAsState()
    val filteredRecipes = savedRecipes.filter { it.cookbookName == cookbookName }

    if (filteredRecipes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recipes in this cookbook yet",
                style = Typography.bodyLarge,
                color = TextSecondaryLight
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredRecipes) { recipe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.selectSavedRecipe(recipe)
                                    navController.navigate("recipe_detail")
                                }) {
                            Text(
                                text = recipe.label,
                                style = Typography.titleMedium,
                            )
                            Text(
                                text = "${recipe.ingredientLines.size} ingredients",
                                style = Typography.bodySmall,
                                color = TextTertiary
                            )
                            Text(
                                text = recipe.ingredientLines.take(3).joinToString(", "),
                                style = Typography.bodySmall,
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(onClick = { viewModel.deleteSavedRecipe(recipe) }) {
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