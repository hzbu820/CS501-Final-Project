package com.cs501.pantrypal.screen.profilePage

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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
val IngredientFormStateSaver: Saver<IngredientFormState, *> = listSaver(save = { state ->
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
}, restore = { items ->
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
})

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientDialog(
    userIngredientsViewModel: UserIngredientsViewModel,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    ingredient: UserIngredients? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Use state class to manage form state
    var formState by rememberSaveable(stateSaver = IngredientFormStateSaver) {
        mutableStateOf(
            if (ingredient != null) {
                IngredientFormState(
                    id = ingredient.id,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    category = ingredient.foodCategory,
                    expirationDate = ingredient.expirationDate,
                    notes = ingredient.notes,
                    isEditing = true
                )
            } else {
                IngredientFormState()
            }
        )
    }

    // UI states
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var isAddingNewCategory by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }
    var filteredIngredients by remember { mutableStateOf<List<UserIngredients>>(emptyList()) }

    // Unit and category options from Constants
    val unitOptions = Constants.MEASUREMENT_UNITS
    val predefinedCategories = Constants.FOOD_CATEGORIES

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
                updateIngredient(
                    formState,
                    userIngredientsViewModel,
                    coroutineScope,
                    snackbarHostState
                )
            } else {
                addNewIngredient(
                    formState,
                    userIngredientsViewModel,
                    coroutineScope,
                    snackbarHostState
                )
            }
            onDismiss()
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (formState.isEditing) "Edit Ingredient" else "Add Ingredient") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ingredient name input with suggestions
                IngredientNameInput(
                    name = formState.name,
                    onNameChange = { formState = formState.copy(name = it); nameError = false },
                    nameError = nameError,
                    snackbarHostState = snackbarHostState,
                    showSuggestions = showSuggestions,
                    filteredIngredients = filteredIngredients,
                    onSelectIngredient = ::selectIngredient,
                    userIngredientsViewModel = userIngredientsViewModel
                )

                // Category selector
                CategorySelector(
                    category = formState.category,
                    predefinedCategories = predefinedCategories,
                    showCategoryMenu = showCategoryMenu,
                    onShowCategoryMenu = { showCategoryMenu = it },
                    onIsAddingNewCategory = { isAddingNewCategory = it },
                    onCategorySelected = { formState = formState.copy(category = it) })

                // Unit and quantity input
                QuantityUnitInput(
                    quantity = formState.quantity,
                    onQuantityChange = {
                        formState = formState.copy(quantity = it); quantityError = false
                    },
                    quantityError = quantityError,
                    unit = formState.unit,
                    unitOptions = unitOptions,
                    showUnitMenu = showUnitMenu,
                    onShowUnitMenu = { showUnitMenu = it },
                    onUnitSelected = { formState = formState.copy(unit = it) })

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {

                    DatePick(
                        onClick = { showDatePicker = true },
                        formState = formState,
                        onValueChange = { formState = formState.copy(expirationDate = it) })

                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()

                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = java.time.Instant.ofEpochMilli(millis)
                                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                        val formattedDate =
                                            "${date.monthValue}-${date.dayOfMonth}-${date.year}"
                                        formState = formState.copy(expirationDate = formattedDate)
                                    }
                                    showDatePicker = false
                                }) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }

                // Notes input
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { formState = formState.copy(notes = it) },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = ::validateAndSubmit, colors = ButtonDefaults.buttonColors(InfoColor)
            ) {
                Text(if (formState.isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}

@Composable
fun DatePick(
    onClick: () -> Unit,
    formState: IngredientFormState,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formState.expirationDate,
            onValueChange = { onValueChange(it) },
            label = { Text("Expiration Date (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Choose Date",
                    tint = InfoColor
                )
            }
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun IngredientNameInput(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: Boolean,
    showSuggestions: Boolean,
    snackbarHostState: SnackbarHostState,
    filteredIngredients: List<UserIngredients>,
    onSelectIngredient: (UserIngredients) -> Unit,
    userIngredientsViewModel: UserIngredientsViewModel
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val coroutineScope = rememberCoroutineScope()

    // Get camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (!isGranted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Camera permission is required to scan barcodes")
                }
            }
        })

    // Launch camera to take picture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(), onResult = { success ->
            if (success && imageUri != null) {
                // Process the image
                try {
                    val image = InputImage.fromFilePath(context, imageUri!!)
                    scanBarcode(image, userIngredientsViewModel, onNameChange) { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: $error")
                        }
                    }
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to process image: ${e.message}")
                    }
                }
            }
        })

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
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = {
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            imageUri = createTempImageUri(context)
                            imageUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode",
                            tint = InfoColor
                        )
                    }
                }
            })

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
                                .padding(16.dp))
                        if (ingredient != filteredIngredients.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

// Create a temporary image URI for the camera
private fun createTempImageUri(context: android.content.Context): Uri? {
    val tempFile = java.io.File.createTempFile(
        "barcode_", ".jpg", context.cacheDir
    ).apply {
        deleteOnExit()
    }
    return androidx.core.content.FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", tempFile
    )
}

private fun scanBarcode(
    image: InputImage,
    userIngredientsViewModel: UserIngredientsViewModel,
    onNameChange: (String) -> Unit,
    onError: (String) -> Unit
) {
    // set barcode scanner options
    val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128
        ).build()

    val scanner = BarcodeScanning.getClient(options)

    scanner.process(image).addOnSuccessListener { barcodes ->
            if (barcodes.isNotEmpty()) {
                val barcode = barcodes[0]
                barcode.rawValue?.let { value ->
                    userIngredientsViewModel.searchIngredientsByApi(value)
                    onNameChange(value)
                }
            }
        }.addOnFailureListener { e ->
            onError("Failed to scan barcode: ${e.message}")
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
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onShowCategoryMenu(true) }
            )

            DropdownMenu(
                expanded = showCategoryMenu, onDismissRequest = { onShowCategoryMenu(false) }) {
                predefinedCategories.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        onCategorySelected(option)
                        onShowCategoryMenu(false)
                        onIsAddingNewCategory(false)
                    })
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
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            ),
            singleLine = true)

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

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onShowUnitMenu(true) }
            )

            DropdownMenu(
                expanded = showUnitMenu, onDismissRequest = { onShowUnitMenu(false) }) {
                unitOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        onUnitSelected(option)
                        onShowUnitMenu(false)
                    })
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