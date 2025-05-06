package com.cs501.pantrypal.screen.profilePage.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.util.Constants
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

// Form state data class
@Stable
data class IngredientFormState(
    val id: Int = 0,
    val name: String = "",
    val category: String = "Default",
    val image: String = "",
    val quantity: String = "",
    val unit: String = "g",
    val expirationDate: String = "",
    val location: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val userId: String = "",
    val isEditing: Boolean = false,

    )

// Custom Saver for IngredientFormState
val IngredientFormStateSaver: Saver<IngredientFormState, *> = listSaver(save = { state ->
    listOf(
        state.id,
        state.name,
        state.category,
        state.image,
        state.quantity,
        state.unit,
        state.expirationDate,
        state.location,
        state.notes,
        state.isFavorite,
        state.userId,
        state.isEditing
    )
}, restore = { items ->
    IngredientFormState(
        id = items[0] as Int,
        name = items[1] as String,
        category = items[2] as String,
        image = items[3] as String,
        quantity = items[4] as String,
        unit = items[5] as String,
        expirationDate = items[6] as String,
        location = items[7] as String,
        notes = items[8] as String,
        isFavorite = items[9] as Boolean,
        userId = items[10] as String,
        isEditing = items[11] as Boolean
    )
})

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IngredientEditDialog(
    userIngredientsViewModel: UserIngredientsViewModel,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState,
    ingredient: UserIngredients? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Use state class to manage form state
    var formState by rememberSaveable(stateSaver = IngredientFormStateSaver) {
        mutableStateOf(
            if (ingredient != null) {
                IngredientFormState(
                    id = ingredient.id,
                    name = ingredient.name,
                    category = ingredient.foodCategory,
                    image = ingredient.image,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    expirationDate = ingredient.expirationDate,
                    location = ingredient.location,
                    notes = ingredient.notes,
                    isFavorite = ingredient.isFavorite,
                    userId = ingredient.userId,
                    isEditing = true
                )
            } else {
                IngredientFormState()
            }
        )
    }

    // UI states
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var isAddingNewCategory by remember { mutableStateOf(false) }

    // For phone mode paging
    var currentPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { 2 }

    // Sync pagerState with currentPage
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    // Unit and category options from Constants
    val unitOptions = Constants.MEASUREMENT_UNITS
    val predefinedCategories = Constants.FOOD_CATEGORIES

    fun validateAndSubmit() {
        nameError = formState.name.isBlank()
        quantityError = formState.quantity.isBlank()

        if (!nameError && !quantityError) {
            if (formState.isEditing) {
                val ingredient = ingredientFormatter(formState, userIngredientsViewModel)

                userIngredientsViewModel.updateIngredient(ingredient)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ingredient updated successfully")
                }

            } else {
                val ingredient = ingredientFormatter(formState, userIngredientsViewModel)

                userIngredientsViewModel.addIngredient(ingredient)

                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Ingredient added successfully")
                }
            }
            onDismiss()
        }
    }

    AlertDialog(onDismissRequest = onDismiss, title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (formState.isEditing) "Edit Ingredient" else "Add Ingredient")
            Spacer(modifier = Modifier.weight(1f))
            if (formState.isEditing) {
                IconButton(onClick = {
                    val ingredient = ingredientFormatter(formState, userIngredientsViewModel)
                    userIngredientsViewModel.deleteIngredient(ingredient)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Ingredient deleted successfully")
                    }
                    formState = IngredientFormState()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

        }
    }, text = {
        if (isTablet) {
            TabletLayout(
                formState = formState,
                onFormStateChange = { formState = it },
                nameError = nameError,
                quantityError = quantityError,
                showDatePicker = showDatePicker,
                onShowDatePicker = { showDatePicker = it },
                showUnitMenu = showUnitMenu,
                onShowUnitMenu = { showUnitMenu = it },
                showCategoryMenu = showCategoryMenu,
                onShowCategoryMenu = { showCategoryMenu = it },
                onIsAddingNewCategory = { isAddingNewCategory = it },
                unitOptions = unitOptions,
                predefinedCategories = predefinedCategories,
                userIngredientsViewModel = userIngredientsViewModel,
                snackbarHostState = snackbarHostState
            )
        } else {
            PhoneLayout(
                formState = formState,
                onFormStateChange = { formState = it },
                nameError = nameError,
                quantityError = quantityError,
                showDatePicker = showDatePicker,
                onShowDatePicker = { showDatePicker = it },
                showUnitMenu = showUnitMenu,
                onShowUnitMenu = { showUnitMenu = it },
                showCategoryMenu = showCategoryMenu,
                onShowCategoryMenu = { showCategoryMenu = it },
                onIsAddingNewCategory = { isAddingNewCategory = it },
                unitOptions = unitOptions,
                predefinedCategories = predefinedCategories,
                userIngredientsViewModel = userIngredientsViewModel,
                snackbarHostState = snackbarHostState,
                pagerState = pagerState
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pagerState.pageCount) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (index == currentPage) InfoColor else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    if (index < pagerState.pageCount - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }, confirmButton = {
        Button(
            onClick = ::validateAndSubmit, colors = ButtonDefaults.buttonColors(InfoColor)
        ) {
            Text(if (formState.isEditing) "Update" else "Add")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

@Composable
fun DatePick(
    onClick: () -> Unit, formState: IngredientFormState, onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = formState.expirationDate,
            onValueChange = { onValueChange(it) },
            label = { Text("Expiration Date (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            singleLine = true,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Choose Date",
                    tint = InfoColor
                )
            })
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
    onBarcodeChange: (String, String) -> Unit,
    nameError: Boolean,
    snackbarHostState: SnackbarHostState,
    userIngredientsViewModel: UserIngredientsViewModel,
) {
    val context = LocalContext.current
    val errorMessage = remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Get camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (!isGranted) {
                coroutineScope.launch {
                    errorMessage.value = "Camera permission is required to scan barcodes"
                }
            }
        })

    // Launch camera to take picture
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && imageUri != null) {
                // Process the image
                try {
                    val image = InputImage.fromFilePath(context, imageUri!!)
                    scanBarcode(
                        image,
                        userIngredientsViewModel,
                        onNameChange,
                        onBarcodeChange
                    ) { error ->
                        coroutineScope.launch {
                            errorMessage.value = error
                        }
                    }
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to process image: ${e.message}")
                    }
                }
            }
        }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                onNameChange(it)
                errorMessage.value = ""
            },
            label = { Text("Ingredient Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError || errorMessage.value.isNotEmpty(),
            supportingText = {
                Column {
                    if (nameError) {
                        Text("Please enter ingredient name", color = Color.Red)
                    }
                    if (errorMessage.value.isNotEmpty()) {
                        Text(errorMessage.value, color = Color.Red)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            singleLine = true,
            trailingIcon = {
                Row {
                    IconButton(onClick = {
                        errorMessage.value = ""
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            imageUri = createTempImageUri(context)
                            imageUri?.let { uri ->
                                cameraLauncher.launch(uri)
                                print("Image URI: $imageUri")
                                println("Image URI: $imageUri")
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "scan barcode",
                            tint = InfoColor
                        )
                    }
                }
            })
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
                    .clickable { onShowCategoryMenu(true) })

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
        var isNumber by remember { mutableStateOf(true) }
        OutlinedTextField(
            value = quantity,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    onQuantityChange(newValue)
                }
                isNumber = newValue.matches(Regex("^\\d*\\.?\\d*$"))
            },
            label = { Text("Quantity") },
            modifier = Modifier.weight(1f),
            isError = quantityError,
            supportingText = {
                Column {
                    if (quantityError) {
                        Text("Please enter quantity", color = Color.Red)
                    }
                    if (!isNumber) {
                        Text("Please Enter Number", color = Color.Red)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
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

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onShowUnitMenu(true) })

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

// Tablet layout - all fields on one page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayout(
    formState: IngredientFormState,
    onFormStateChange: (IngredientFormState) -> Unit,
    nameError: Boolean,
    quantityError: Boolean,
    showDatePicker: Boolean,
    onShowDatePicker: (Boolean) -> Unit,
    showUnitMenu: Boolean,
    onShowUnitMenu: (Boolean) -> Unit,
    showCategoryMenu: Boolean,
    onShowCategoryMenu: (Boolean) -> Unit,
    onIsAddingNewCategory: (Boolean) -> Unit,
    unitOptions: List<String>,
    predefinedCategories: List<String>,
    userIngredientsViewModel: UserIngredientsViewModel,
    snackbarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // First row: Name and Category
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                // Ingredient name input with suggestions
                IngredientNameInput(
                    name = formState.name,
                    onNameChange = { onFormStateChange(formState.copy(name = it)) },
                    onBarcodeChange = { name, img ->
                        onFormStateChange(formState.copy(image = img, name = name))
                    },
                    nameError = nameError,
                    snackbarHostState = snackbarHostState,
                    userIngredientsViewModel = userIngredientsViewModel
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Category selector
                CategorySelector(
                    category = formState.category,
                    predefinedCategories = predefinedCategories,
                    showCategoryMenu = showCategoryMenu,
                    onShowCategoryMenu = onShowCategoryMenu,
                    onIsAddingNewCategory = onIsAddingNewCategory,
                    onCategorySelected = { onFormStateChange(formState.copy(category = it)) })
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                IconButton(onClick = {
                    onFormStateChange(formState.copy(isFavorite = !formState.isFavorite))
                }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = if (formState.isFavorite) Color.Red else Color.Gray
                    )
                }
            }
        }

        Row(
            modifier = Modifier, verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity and Unit
            QuantityUnitInput(
                quantity = formState.quantity,
                onQuantityChange = { onFormStateChange(formState.copy(quantity = it)) },
                quantityError = quantityError,
                unit = formState.unit,
                unitOptions = unitOptions,
                showUnitMenu = showUnitMenu,
                onShowUnitMenu = onShowUnitMenu,
                onUnitSelected = { onFormStateChange(formState.copy(unit = it)) })
        }

        // Third row: Date picker
        Row(
            modifier = Modifier, verticalAlignment = Alignment.CenterVertically
        ) {
            DatePick(
                onClick = { onShowDatePicker(true) },
                formState = formState,
                onValueChange = { onFormStateChange(formState.copy(expirationDate = it)) })

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()

                DatePickerDialog(onDismissRequest = { onShowDatePicker(false) }, confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val formattedDate = "${date.monthValue}-${date.dayOfMonth}-${date.year}"
                            onFormStateChange(formState.copy(expirationDate = formattedDate))
                        }
                        onShowDatePicker(false)
                    }) {
                        Text("Confirm")
                    }
                }, dismissButton = {
                    TextButton(onClick = { onShowDatePicker(false) }) {
                        Text("Cancel")
                    }
                }) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        // Fourth row: Location
        OutlinedTextField(
            value = formState.location,
            onValueChange = { onFormStateChange(formState.copy(location = it)) },
            label = { Text("Storage Location (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        // Fifth row: Notes
        OutlinedTextField(
            value = formState.notes,
            onValueChange = { onFormStateChange(formState.copy(notes = it)) },
            label = { Text("Notes (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
            )
        )
    }
}

// Phone layout - two pages with swipe
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhoneLayout(
    formState: IngredientFormState,
    onFormStateChange: (IngredientFormState) -> Unit,
    nameError: Boolean,
    quantityError: Boolean,
    showDatePicker: Boolean,
    onShowDatePicker: (Boolean) -> Unit,
    showUnitMenu: Boolean,
    onShowUnitMenu: (Boolean) -> Unit,
    showCategoryMenu: Boolean,
    onShowCategoryMenu: (Boolean) -> Unit,
    onIsAddingNewCategory: (Boolean) -> Unit,
    unitOptions: List<String>,
    predefinedCategories: List<String>,
    userIngredientsViewModel: UserIngredientsViewModel,
    snackbarHostState: SnackbarHostState,
    pagerState: PagerState,

    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        // Pager for swipe navigation
        HorizontalPager(
            state = pagerState, modifier = Modifier.height(300.dp)
        ) { page ->
            when (page) {
                0 -> {
                    // First page - Main fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(0.9f)) {
                                IngredientNameInput(
                                    name = formState.name,
                                    onNameChange = { onFormStateChange(formState.copy(name = it)) },
                                    onBarcodeChange = { name, img ->
                                        onFormStateChange(formState.copy(image = img, name = name))
                                    },
                                    nameError = nameError,
                                    snackbarHostState = snackbarHostState,
                                    userIngredientsViewModel = userIngredientsViewModel
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(0.1f)) {
                                IconButton(onClick = {
                                    onFormStateChange(formState.copy(isFavorite = !formState.isFavorite))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorite",
                                        tint = if (formState.isFavorite) Color.Red else Color.Gray
                                    )
                                }
                            }
                        }


                        // Category
                        CategorySelector(
                            category = formState.category,
                            predefinedCategories = predefinedCategories,
                            showCategoryMenu = showCategoryMenu,
                            onShowCategoryMenu = onShowCategoryMenu,
                            onIsAddingNewCategory = onIsAddingNewCategory,
                            onCategorySelected = { onFormStateChange(formState.copy(category = it)) })

                        // Quantity and Unit
                        QuantityUnitInput(
                            quantity = formState.quantity,
                            onQuantityChange = { onFormStateChange(formState.copy(quantity = it)) },
                            quantityError = quantityError,
                            unit = formState.unit,
                            unitOptions = unitOptions,
                            showUnitMenu = showUnitMenu,
                            onShowUnitMenu = onShowUnitMenu,
                            onUnitSelected = { onFormStateChange(formState.copy(unit = it)) })

                        // page indicator two dots

                    }
                }

                1 -> {
                    // Second page - Additional fields
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Expiration Date
                        DatePick(
                            onClick = { onShowDatePicker(true) },
                            formState = formState,
                            onValueChange = { onFormStateChange(formState.copy(expirationDate = it)) })

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState()

                            DatePickerDialog(
                                onDismissRequest = { onShowDatePicker(false) },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val date = Instant.ofEpochMilli(millis)
                                                .atZone(ZoneId.systemDefault()).toLocalDate()
                                            val formattedDate =
                                                "${date.monthValue}-${date.dayOfMonth}-${date.year}"
                                            onFormStateChange(formState.copy(expirationDate = formattedDate))
                                        }
                                        onShowDatePicker(false)
                                    }) {
                                        Text("Confirm")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { onShowDatePicker(false) }) {
                                        Text("Cancel")
                                    }
                                }) {
                                DatePicker(state = datePickerState)
                            }
                        }

                        // Location
                        OutlinedTextField(
                            value = formState.location,
                            onValueChange = { onFormStateChange(formState.copy(location = it)) },
                            label = { Text("Storage Location (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )

                        // Notes
                        OutlinedTextField(
                            value = formState.notes,
                            onValueChange = { onFormStateChange(formState.copy(notes = it)) },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun ingredientFormatter(
    formState: IngredientFormState,
    viewModel: UserIngredientsViewModel,
): UserIngredients {

    val updatedIngredient = UserIngredients(
        id = formState.id,
        name = formState.name.trim(),
        foodCategory = formState.category,
        image = formState.image,
        quantity = formState.quantity.trim(),
        unit = formState.unit,
        expirationDate = formState.expirationDate.trim(),
        location = formState.location,
        notes = formState.notes.trim(),
        isFavorite = formState.isFavorite,
        userId = viewModel.getCurrentUserId()
    )
    return updatedIngredient
}