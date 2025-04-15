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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs501.pantrypal.data.database.GroceryItem
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.TextSecondary
import com.cs501.pantrypal.ui.theme.TextTertiary
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.viewmodel.GroceryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    groceryViewModel: GroceryViewModel,
    snackbarHostState: SnackbarHostState
) {
    val allGroceryItems by groceryViewModel.allGroceryItems.collectAsState()
    val showCheckedItems by groceryViewModel.showCheckedItems.collectAsState()
    val searchQuery by groceryViewModel.searchQuery.collectAsState()
    val categoryFilter by groceryViewModel.categoryFilter.collectAsState()
    val scope = rememberCoroutineScope()

    // UI state
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showFilterOptions by remember { mutableStateOf(false) }
    var tempSearchQuery by remember { mutableStateOf(searchQuery) }

    // Check if device is a tablet based on screen width
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    if (isTablet) {
        // Tablet layout with categories sidebar
        TabletGroceryLayout(
            allGroceryItems = allGroceryItems,
            showCheckedItems = showCheckedItems,
            searchQuery = tempSearchQuery,
            categoryFilter = categoryFilter,
            onSearchQueryChange = { query ->
                tempSearchQuery = query
                groceryViewModel.searchGroceryItems(query)
            },
            onClearSearch = {
                tempSearchQuery = ""
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
            searchQuery = tempSearchQuery,
            showFilterOptions = showFilterOptions,
            onSearchQueryChange = { query ->
                tempSearchQuery = query
                groceryViewModel.searchGroceryItems(query)
            },
            onClearSearch = {
                tempSearchQuery = ""
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
                .width(240.dp)
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

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Search") },
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
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear completed",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Clear Completed")
                }

                // Categories header
                Text(
                    text = "Categories",
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // All categories option
                CategoryItem(
                    category = "All Items",
                    isSelected = categoryFilter == null,
                    onCategorySelect = { onCategorySelect(null) })

                // Category list
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
                Text(
                    text = categoryFilter,
                    style = Typography.displayMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                )
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
            imageVector = when (category) {
                "All Items" -> Icons.AutoMirrored.Filled.List
                "Vegetables" -> Icons.Default.Eco
                "Meat" -> Icons.Default.Restaurant
                "Dairy" -> Icons.Default.Opacity
                "Grains" -> Icons.Default.Grain
                "Seafood" -> Icons.Default.Water
                "Spices" -> Icons.Default.Whatshot
                "Beverages" -> Icons.Default.LocalDrink
                "Snacks" -> Icons.Default.Cookie
                else -> Icons.Default.Category
            },
            contentDescription = category,
            tint = textColor,
            modifier = Modifier.padding(end = 12.dp)
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

        // Search and Filter Bar
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
                EmptyGroceryList(onAddItem = onShowAddItemDialog)
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

    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .focusRequester(focusRequester),
        placeholder = { Text("Search") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search, contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = onClearSearch) {
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
            keyboardController?.hide()
        }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary
        )
    )
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

            // Delete button
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
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    // Unit dropdown options from Constants
    val unitOptions = Constants.MEASUREMENT_UNITS
    var expandedUnitMenu by remember { mutableStateOf(false) }

    // Category dropdown options from Constants
    val categoryOptions = Constants.FOOD_CATEGORIES
    var expandedCategoryMenu by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank()

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
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
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
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(contentColor = InfoColor)
        ) {
            Text("Cancel")
        }
    })
}