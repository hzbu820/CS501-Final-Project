package com.cs501.pantrypal.screen.profilePage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Form state data class
@Stable
data class IngredientFormState(
    val id: Int = 0,
    val name: String = "",
    val quantity: String = "",
    val unit: String = "g",
    val category: String = "",
    val expirationDate: String = "",
    val notes: String = "",
    val isEditing: Boolean = false
)

// Custom Saver for IngredientFormState
val IngredientFormStateSaver: Saver<IngredientFormState, *> = listSaver(
    save = { state ->
        listOf(
            state.id,
            state.name,
            state.quantity,
            state.unit,
            state.category,
            state.expirationDate,
            state.notes,
            state.isEditing
        )
    },
    restore = { items ->
        IngredientFormState(
            id = items[0] as Int,
            name = items[1] as String,
            quantity = items[2] as String,
            unit = items[3] as String,
            category = items[4] as String,
            expirationDate = items[5] as String,
            notes = items[6] as String,
            isEditing = items[7] as Boolean
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    userIngredientsViewModel: UserIngredientsViewModel,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    // Use state class to manage form state
    var formState by rememberSaveable(stateSaver = IngredientFormStateSaver) {
        mutableStateOf(IngredientFormState())
    }

    // UI states
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var isAddingNewCategory by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var filteredIngredients by remember { mutableStateOf<List<UserIngredients>>(emptyList()) }

    // Unit and category options
    val unitOptions = listOf("g", "kg", "ml", "L", "oz", "lb", "gal")
    val predefinedCategories = listOf(
        "Fruits", "Vegetables", "Meat", "Dairy", "Grains",
        "Seafood", "Spices", "Beverages", "Snacks", "Other"
    )

    // Show suggestions based on user input
    LaunchedEffect(formState.name) {
        if (formState.name.isNotBlank()) {
            userIngredientsViewModel.searchIngredients(formState.name)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }

    // Collect ingredients related to user input
    LaunchedEffect(Unit) {
        userIngredientsViewModel.ingredients.collectLatest { ingredients ->
            if (formState.name.isBlank()) {
                filteredIngredients = emptyList()
            } else {
                filteredIngredients = ingredients.filter {
                    it.name.contains(formState.name, ignoreCase = true)
                }
            }
        }
    }

    fun selectIngredient(ingredient: UserIngredients) {
        formState = formState.copy(
            id = ingredient.id,
            name = ingredient.name,
            quantity = ingredient.quantity,
            unit = ingredient.unit,
            category = ingredient.foodCategory,
            expirationDate = ingredient.expirationDate,
            notes = ingredient.notes,
            isEditing = true
        )
        showSuggestions = false
    }

    fun validateAndSubmit() {
        nameError = formState.name.isBlank()
        quantityError = formState.quantity.isBlank()

        if (!nameError && !quantityError) {
            if (formState.isEditing) {
                updateIngredient(formState, userIngredientsViewModel, coroutineScope, snackbarHostState)
            } else {
                addNewIngredient(formState, userIngredientsViewModel, coroutineScope, snackbarHostState)
            }
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditing) "Edit Ingredient" else "Add Ingredient") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Ingredient name input with suggestions
            IngredientNameInput(
                name = formState.name,
                onNameChange = { formState = formState.copy(name = it); nameError = false },
                nameError = nameError,
                showSuggestions = showSuggestions,
                filteredIngredients = filteredIngredients,
                onSelectIngredient = ::selectIngredient
            )

            // Category selector
            CategorySelector(
                category = formState.category,
                predefinedCategories = predefinedCategories,
                showCategoryMenu = showCategoryMenu,
                onShowCategoryMenu = { showCategoryMenu = it },
                onIsAddingNewCategory = { isAddingNewCategory = it },
                onCategorySelected = { formState = formState.copy(category = it) }
            )

            // Unit and quantity input
            QuantityUnitInput(
                quantity = formState.quantity,
                onQuantityChange = { formState = formState.copy(quantity = it); quantityError = false },
                quantityError = quantityError,
                unit = formState.unit,
                unitOptions = unitOptions,
                showUnitMenu = showUnitMenu,
                onShowUnitMenu = { showUnitMenu = it },
                onUnitSelected = { formState = formState.copy(unit = it) }
            )

            // Expiration date input
            OutlinedTextField(
                value = formState.expirationDate,
                onValueChange = { formState = formState.copy(expirationDate = it) },
                label = { Text("Expiration Date(Optional, Format: MM-DD-YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            // Notes input
            OutlinedTextField(
                value = formState.notes,
                onValueChange = { formState = formState.copy(notes = it) },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = ::validateAndSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (formState.isEditing) "Update Ingredient" else "Add Ingredient")
            }
        }
    }
}

@Composable
fun IngredientNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: Boolean,
    showSuggestions: Boolean,
    filteredIngredients: List<UserIngredients>,
    onSelectIngredient: (UserIngredients) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Ingredient Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError,
            supportingText = if (nameError) {
                { Text("Please enter ingredient name") }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Ingredient"
                )
            }
        )

        if (showSuggestions && filteredIngredients.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filteredIngredients) { ingredient ->
                        Text(
                            text = ingredient.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectIngredient(ingredient) }
                                .padding(16.dp)
                        )
                        if (ingredient != filteredIngredients.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelector(
    category: String,
    predefinedCategories: List<String>,
    showCategoryMenu: Boolean,
    onShowCategoryMenu: (Boolean) -> Unit,
    onIsAddingNewCategory: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = category,
                onValueChange = { },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { onShowCategoryMenu(true) }) {
                        Icon(Icons.Filled.ArrowDropDown, "Select Category")
                    }
                },
                singleLine = true
            )

            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { onShowCategoryMenu(false) }
            ) {
                predefinedCategories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCategorySelected(option)
                            onShowCategoryMenu(false)
                            onIsAddingNewCategory(false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityUnitInput(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    quantityError: Boolean,
    unit: String,
    unitOptions: List<String>,
    showUnitMenu: Boolean,
    onShowUnitMenu: (Boolean) -> Unit,
    onUnitSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            label = { Text("Quantity") },
            modifier = Modifier.weight(1f),
            isError = quantityError,
            supportingText = if (quantityError) {
                { Text("Please enter quantity") }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            OutlinedTextField(
                value = unit,
                onValueChange = { },
                label = { Text("Unit") },
                modifier = Modifier.width(100.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { onShowUnitMenu(true) }) {
                        Icon(Icons.Filled.ArrowDropDown, "Select Unit")
                    }
                },
                singleLine = true
            )

            DropdownMenu(
                expanded = showUnitMenu,
                onDismissRequest = { onShowUnitMenu(false) }
            ) {
                unitOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onUnitSelected(option)
                            onShowUnitMenu(false)
                        }
                    )
                }
            }
        }
    }
}

private fun addNewIngredient(
    formState: IngredientFormState,
    viewModel: UserIngredientsViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val newIngredient = UserIngredients(
        name = formState.name.trim(),
        quantity = formState.quantity.trim(),
        unit = formState.unit,
        foodCategory = formState.category,
        expirationDate = formState.expirationDate.trim(),
        notes = formState.notes.trim(),
        userId = viewModel.getCurrentUserId()
    )

    viewModel.addIngredient(newIngredient)

    coroutineScope.launch {
        snackbarHostState.showSnackbar("Ingredient added successfully")
    }
}

private fun updateIngredient(
    formState: IngredientFormState,
    viewModel: UserIngredientsViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val updatedIngredient = UserIngredients(
        id = formState.id,
        name = formState.name.trim(),
        quantity = formState.quantity.trim(),
        unit = formState.unit,
        foodCategory = formState.category,
        expirationDate = formState.expirationDate.trim(),
        notes = formState.notes.trim(),
        userId = viewModel.getCurrentUserId()
    )

    viewModel.updateIngredient(updatedIngredient)

    coroutineScope.launch {
        snackbarHostState.showSnackbar("Ingredient updated successfully")
    }
}