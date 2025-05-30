package com.cs501.pantrypal.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs501.pantrypal.AppViewModelProvider
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.GroceryItem
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.TextSecondary
import com.cs501.pantrypal.ui.theme.TextTertiary
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.util.Constants.FOOD_IMAGES
import com.cs501.pantrypal.viewmodel.GroceryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    snackbarHostState: SnackbarHostState
) {
    val groceryViewModel: GroceryViewModel = AppViewModelProvider.groceryViewModel
    val allGroceryItems by groceryViewModel.allGroceryItems.collectAsState()
    val showCheckedItems by groceryViewModel.showCheckedItems.collectAsState()
    val searchQuery by groceryViewModel.searchQuery.collectAsState()
    val categoryFilter by groceryViewModel.categoryFilter.collectAsState()
    val scope = rememberCoroutineScope()

    // UI state
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }

    // Check if device is a tablet based on screen width
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (isTablet) {
        // Tablet layout with categories sidebar
        TabletGroceryLayout(
            allGroceryItems = allGroceryItems,
            showCheckedItems = showCheckedItems,
            searchQuery = searchQuery,
            categoryFilter = categoryFilter,
            onSearchQueryChange = { query ->
                // Now only perform search when explicitly requested via button click
                groceryViewModel.searchGroceryItems(query)
            },
            onClearSearch = {
                groceryViewModel.searchGroceryItems("")
            },
            onToggleShowCheckedItems = { groceryViewModel.toggleShowCheckedItems() },
            onCategorySelect = { groceryViewModel.setCategoryFilter(it) },
            onToggleItem = { groceryViewModel.toggleItemChecked(it) },
            onDeleteItem = { groceryViewModel.deleteGroceryItem(it) },
            onClearCheckedItems = {
                scope.launch {
                    groceryViewModel.clearCheckedItems()
                    snackbarHostState.showSnackbar("Cleared completed items")
                }
            },
            onShowAddItemDialog = { showAddItemDialog = !showAddItemDialog },
        )
    } else {
        // Phone layout
        PhoneGroceryLayout(
            allGroceryItems = allGroceryItems,
            showCheckedItems = showCheckedItems,
            searchQuery = searchQuery,
            showFilterOptions = showFilterOptions,
            onSearchQueryChange = { query ->
                // Now only perform search when explicitly requested via button click
                groceryViewModel.searchGroceryItems(query)
            },
            onClearSearch = {
                groceryViewModel.searchGroceryItems("")
            },
            onToggleFilterOptions = { showFilterOptions = !showFilterOptions },
            onToggleShowCheckedItems = { groceryViewModel.toggleShowCheckedItems() },
            onToggleItem = { groceryViewModel.toggleItemChecked(it) },
            onDeleteItem = { groceryViewModel.deleteGroceryItem(it) },
            onClearCheckedItems = {
                scope.launch {
                    groceryViewModel.clearCheckedItems()
                    snackbarHostState.showSnackbar("Cleared completed items")
                }
            },
            onShowAddItemDialog = { showAddItemDialog = !showAddItemDialog },
        )
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddGroceryItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAddItem = { name, quantity, unit, category ->
                groceryViewModel.addGroceryItem(name, quantity, unit, category)
                showAddItemDialog = false
            })
    }
}

@Composable
fun TabletGroceryLayout(
    allGroceryItems: List<GroceryItem>,
    showCheckedItems: Boolean,
    searchQuery: String,
    categoryFilter: String?,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleShowCheckedItems: () -> Unit,
    onCategorySelect: (String?) -> Unit,
    onToggleItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit,
    onClearCheckedItems: () -> Unit,
    onShowAddItemDialog: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left sidebar with categories and search
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
                    text = "Grocery List",
                    style = Typography.displayMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Search bar with button
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onClearSearch = onClearSearch
                )

                // Filter options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Show completed", style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = showCheckedItems,
                        onCheckedChange = { onToggleShowCheckedItems() })
                }

                // Clear completed button
                Button(
                    onClick = onClearCheckedItems,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor, contentColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear completed",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Clear Completed")
                }

                // Categories header with selected category info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = Typography.headlineMedium
                    )
                    
                    // Show current filter if any
                    if (categoryFilter != null) {
                        Text(
                            text = "Current: $categoryFilter",
                            style = MaterialTheme.typography.bodySmall,
                            color = InfoColor,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // All categories option is always visible
                CategoryItem(
                    category = "All Items",
                    isSelected = categoryFilter == null,
                    onCategorySelect = { onCategorySelect(null) })
                
                // Category list - always visible regardless of current filter
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(Constants.FOOD_CATEGORIES) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = categoryFilter == category,
                            onCategorySelect = { onCategorySelect(category) })
                    }
                }
            }
        }

        // Right content area with grocery items
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Category title if selected
            if (categoryFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = FOOD_IMAGES[categoryFilter] ?: R.drawable.grocery),
                        contentDescription = categoryFilter,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 16.dp),
                        tint = Color.Unspecified
                    )
                    
                    Text(
                        text = categoryFilter,
                        style = Typography.displayMedium
                    )
                }
            }

            // Grocery list
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (allGroceryItems.isEmpty()) {
                    EmptyGroceryList(onShowAddItemDialog)
                } else {
                    GroceryItemsList(
                        groceryItems = allGroceryItems,
                        onToggleItem = onToggleItem,
                        onDeleteItem = onDeleteItem
                    )
                }

                // Add button
                FloatingActionButton(
                    onClick = onShowAddItemDialog,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = InfoColor,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add Item",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: String, isSelected: Boolean, onCategorySelect: () -> Unit
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
            .clickable { onCategorySelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically) {

        Icon(
            painter = painterResource(id = FOOD_IMAGES[category] ?: R.drawable.grocery),
            contentDescription = category,
            modifier = Modifier
                .size(35.dp)
                .padding(end = 12.dp),
            tint = Color.Unspecified
        )

        Text(
            text = category, style = MaterialTheme.typography.bodyLarge, color = textColor
        )
    }
}

@Composable
fun PhoneGroceryLayout(
    allGroceryItems: List<GroceryItem>,
    showCheckedItems: Boolean,
    searchQuery: String,
    showFilterOptions: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleFilterOptions: () -> Unit,
    onToggleShowCheckedItems: () -> Unit,
    onToggleItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit,
    onClearCheckedItems: () -> Unit,
    onShowAddItemDialog: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grocery List", style = Typography.displayLarge
            )

            Row {
                // Filter button
                IconButton(onClick = onToggleFilterOptions) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Filter",
                        tint = InfoColor
                    )
                }

                // Clear completed button
                IconButton(onClick = onClearCheckedItems) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear completed",
                        tint = ErrorColor
                    )
                }
            }
        }

        // Search Bar with button (modified)
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onClearSearch = onClearSearch
        )

        // Filter Options
        AnimatedVisibility(
            visible = showFilterOptions,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            FilterOptions(
                showCheckedItems = showCheckedItems,
                onToggleShowCheckedItems = onToggleShowCheckedItems
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Grocery List
            if (allGroceryItems.isEmpty()) {
                EmptyGroceryList(onShowAddItemDialog)
            } else {
                GroceryItemsList(
                    groceryItems = allGroceryItems,
                    onToggleItem = onToggleItem,
                    onDeleteItem = onDeleteItem
                )
            }

            // Add button
            FloatingActionButton(
                onClick = onShowAddItemDialog,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = InfoColor,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = "Add Item",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String, onSearchQueryChange: (String) -> Unit, onClearSearch: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var tempQuery by remember { mutableStateOf(searchQuery) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = tempQuery,
            onValueChange = { tempQuery = it },
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search, contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (tempQuery.isNotEmpty()) {
                    IconButton(onClick = { 
                        tempQuery = ""
                        onClearSearch() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear, contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                onSearchQueryChange(tempQuery)
                keyboardController?.hide()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )
        
        // Search button
        IconButton(
            onClick = { 
                onSearchQueryChange(tempQuery)
                keyboardController?.hide()
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Execute search",
                tint = InfoColor
            )
        }
    }
}

@Composable
fun FilterOptions(
    showCheckedItems: Boolean, onToggleShowCheckedItems: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Show completed items", style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = showCheckedItems, onCheckedChange = { onToggleShowCheckedItems() })
        }
    }
}

@Composable
fun GroceryItemsList(
    groceryItems: List<GroceryItem>,
    onToggleItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groceryItems) { item ->
            GroceryItemCard(
                groceryItem = item, onToggleItem = onToggleItem, onDeleteItem = onDeleteItem
            )
        }
        // Add bottom padding for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun GroceryItemCard(
    groceryItem: GroceryItem,
    onToggleItem: (GroceryItem) -> Unit,
    onDeleteItem: (GroceryItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox
            Checkbox(
                checked = groceryItem.isChecked,
                onCheckedChange = { onToggleItem(groceryItem) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )

            // Item details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = groceryItem.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textDecoration = if (groceryItem.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (groceryItem.isChecked) MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.6f
                    )
                    else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category pill
                    if (groceryItem.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = groceryItem.category,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Quantity and unit
                    if (groceryItem.quantity.isNotBlank()) {
                        Text(
                            text = "${groceryItem.quantity} ${groceryItem.unit}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (groceryItem.isChecked) {
                TextButton(onClick = { addToPantry(groceryItem) }) {

                    Text("Add to Pantry", color = InfoColor)
                }
            } else {
                IconButton(onClick = { onDeleteItem(groceryItem) }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

fun ingredientFormatter(groceryItem: GroceryItem): UserIngredients {
    return UserIngredients(
        name = groceryItem.name,
        foodCategory = groceryItem.category,
        quantity = groceryItem.quantity,
        unit = groceryItem.unit,
        expirationDate = "",
        userId = groceryItem.userId
    )
}

fun addToPantry(groceryItem: GroceryItem) {
    val userIngredient = ingredientFormatter(groceryItem)
    val userIngredientsViewModel = AppViewModelProvider.userIngredientsViewModel
    val groceryViewModel = AppViewModelProvider.groceryViewModel
    userIngredientsViewModel.addIngredient(userIngredient)
    groceryViewModel.deleteGroceryItem(groceryItem)
}

@Composable
fun EmptyGroceryList(onAddItem: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "Empty grocery list",
            modifier = Modifier.size(120.dp),
            tint = InfoColor.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your grocery list is empty",
            style = Typography.displayMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first item to get started",
            style = Typography.bodyMedium,
            color = TextTertiary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroceryItemDialog(
    onDismiss: () -> Unit,
    onAddItem: (name: String, quantity: String, unit: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var category by remember { mutableStateOf("Default") }

    // Unit dropdown options from Constants
    val unitOptions = Constants.MEASUREMENT_UNITS
    var expandedUnitMenu by remember { mutableStateOf(false) }

    // Category dropdown options from Constants
    val categoryOptions = Constants.FOOD_CATEGORIES
    var expandedCategoryMenu by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank()
    var isNumeric by remember { mutableStateOf(true) }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Text(
            text = "Add Grocery Item",
            style = Typography.headlineLarge,
        )
    }, text = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )

            // Quantity field
            OutlinedTextField(
                value = quantity,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        quantity = newValue
                        isNumeric = true
                    } else isNumeric = false
                                },
                label = { Text("Quantity") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                supportingText = {
                    if (!isNumeric) {
                        Text(
                            text = "Please enter a valid number",
                            color = ErrorColor,
                            style = Typography.bodySmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                )
            )

            // Unit field with dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedUnitMenu = !expandedUnitMenu }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Show units"
                            )
                        }
                    })

                DropdownMenu(
                    expanded = expandedUnitMenu,
                    onDismissRequest = { expandedUnitMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    unitOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            unit = option
                            expandedUnitMenu = false
                        })
                    }
                }
            }

            // Category field with dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedCategoryMenu = !expandedCategoryMenu }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Show categories"
                            )
                        }
                    })

                DropdownMenu(
                    expanded = expandedCategoryMenu,
                    onDismissRequest = { expandedCategoryMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            category = option
                            expandedCategoryMenu = false
                        })
                    }
                }
            }
        }
    }, confirmButton = {
        Button(
            onClick = {
                if (isFormValid) {
                    onAddItem(name, quantity, unit, category)
                }
            }, enabled = isFormValid, colors = ButtonDefaults.buttonColors(InfoColor)
        ) {
            Text("Add")
        }
    }, dismissButton = {
        TextButton(
            onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = InfoColor)
        ) {
            Text("Cancel")
        }
    })
}