package com.cs501.pantrypal.screen.profilePage

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Sync
import com.cs501.pantrypal.data.firebase.FirebaseService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.*
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.launch
import com.cs501.pantrypal.ui.theme.Typography
import java.io.File
import java.util.Locale

@SuppressLint("SimpleDateFormat", "UnusedBoxWithConstraintsScope")
@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    userIngredientsViewModel: UserIngredientsViewModel
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val ingredients by userIngredientsViewModel.allIngredients.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // State for profile image URI
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var showEditOptions by remember { mutableStateOf(false) }
    var showAddIngredientDialog by remember { mutableStateOf(false) }

    // Image picker launcher
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it
            coroutineScope.launch {
                // Save the image to internal storage
                val fileName = "profile_${userViewModel.currentUser.value?.id}.jpg"
                val fileDir = context.filesDir
                val file = File(fileDir, fileName)
                
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val currentUser = userViewModel.currentUser.value
                    if (currentUser != null) {
                        val updatedUser = currentUser.copy(
                            profileImageUrl = file.absolutePath
                        )
                        userViewModel.updateUserProfile(updatedUser)
                        snackbarHostState.showSnackbar("Profile image updated and saved")
                    } else {
                        snackbarHostState.showSnackbar("Failed to update profile: User not logged in")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Failed to save image: ${e.message}")
                }
            }
        }
    }

    if (showAddIngredientDialog) {
        AddIngredientDialog(
            userIngredientsViewModel = userIngredientsViewModel,
            onDismiss = { showAddIngredientDialog = false },
            snackbarHostState = snackbarHostState
        )
    }

    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (userViewModel.isLoggedIn && currentUser != null) {
            val user = currentUser!!

            var isSyncing by remember { mutableStateOf(false) }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProfileTopBar(userViewModel.isLoggedIn){
                        userViewModel.logout()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Logout successful", duration = SnackbarDuration.Long)
                        }
                        navController.navigate("discover")
                    }
                    ProfileContent(
                        user = user,
                        profileImageUri = profileImageUri,
                        showEditOptions = showEditOptions,
                        ingredients = ingredients,
                        onToggleEditOptions = { showEditOptions = !showEditOptions },
                        onSelectImage = { imagePickerLauncher.launch("image/*") },
                        onNavigateToAddIngredient = { showAddIngredientDialog = true },
                        onSyncClick = {
                            coroutineScope.launch {
                                isSyncing = true
                                try {
                                    val firebaseService = FirebaseService()
                                    val ingredientsSynced = firebaseService.restoreUserIngredients(user.id)

                                    userIngredientsViewModel.updateAllIngredients(ingredientsSynced)
                                    
                                    isSyncing = false
                                    
                                    if (ingredientsSynced != emptyList<UserIngredients>()) {
                                        snackbarHostState.showSnackbar("Data synced successfully")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to sync data")
                                    }
                                } catch (e: Exception) {
                                    isSyncing = false
                                    snackbarHostState.showSnackbar("Sync Error: ${e.message}")
                                }
                            }
                        },
                        navController = navController,
                        userViewModel = userViewModel
                    )
                }

                if (isSyncing) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(color = Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = InfoColor
                        )
                    }
                }
            }
        } else {
            NotLoggedInContent{ navController.navigate("login") }
        }
    }
}

@Composable
private fun ProfileTopBar(
    isLoggedIn: Boolean,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profile",
            style = Typography.displayLarge,
        )

        if (isLoggedIn) {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = ErrorColor
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    profileImageUri: Uri?,
    showEditOptions: Boolean,
    ingredients: List<UserIngredients>,
    onToggleEditOptions: () -> Unit,
    onSelectImage: () -> Unit,
    onNavigateToAddIngredient: () -> Unit,
    onSyncClick: () -> Unit = {},
    navController: NavController,
    userViewModel: UserViewModel
) {
    // User Info Section
    UserInfoSection(user, profileImageUri, onToggleEditOptions, onSelectImage, onSyncClick)

    // Edit User Info Section
    if (showEditOptions) {
        EditOptionsCard(user, ingredients, navController, userViewModel)
    }

    // User Pantry Section
    MyPantrySection(ingredients, onNavigateToAddIngredient)
}

@Composable
private fun UserInfoSection(
    user: User,
    profileImageUri: Uri?,
    onEditClick: () -> Unit,
    onImageClick: () -> Unit,
    onSyncClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BackgroundLight)
                .clickable(onClick = onImageClick),
            contentAlignment = Alignment.Center
        ) {
            when {
                profileImageUri != null -> {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                user.profileImageUrl.isNotBlank() -> {
                    val imageFile = File(user.profileImageUrl)
                    if (imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image),
                            contentDescription = "Default Profile Image",
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile_image),
                        contentDescription = "Default Profile Image",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }

        // User Info
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = user.username,
                style = Typography.headlineLarge
            )

            Text(
                text = if (user.email.isNotBlank()) user.email else "No email provided",
                style = Typography.bodyMedium
            )

            Text(
                text = "Join PantryPal Since: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(user.createdAt))}",
                style = Typography.bodySmall,
            )
        }

        // Edit Button
        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edit Profile",
                tint = InfoColor
            )
        }

        // Sync Button
        IconButton(onClick = { onSyncClick() }) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync to Cloud",
                tint = InfoColor
            )
        }
    }
}

@Composable
private fun MyPantrySection(
    ingredients: List<UserIngredients>,
    onAddIngredient: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Pantry",
                    style = Typography.titleLarge
                )

                IconButton(onClick = onAddIngredient) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Ingredient",
                        tint = InfoColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (ingredients.isEmpty()) {
                EmptyPantryMessage()
            } else {
                IngredientsRow(ingredients)
            }
        }
    }
}

@Composable
private fun EmptyPantryMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No ingredients in your pantry yet",
            style = Typography.titleSmall
        )
    }
}

@Composable
private fun IngredientsRow(ingredients: List<UserIngredients>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        items(ingredients.size) { index ->
            IngredientItem(ingredients[index])
        }
    }
}

@Composable
private fun NotLoggedInContent(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.default_profile_image),
                contentDescription = "Default Profile",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "You are not logged in",
                style = Typography.titleMedium
            )

            Text(
                text = "Please login to view your profile",
                style = Typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Login")
            }
        }
    }
}

@Composable
fun IngredientItem(ingredient: UserIngredients) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .padding(end = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ingredient Image
            //TODO: Replace with actual image loading logic
            Image(
                painter = painterResource(id = R.drawable.default_ingredient_image),
                contentDescription = ingredient.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundLight)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Ingredient Name
            Text(
                text = ingredient.name,
                style = Typography.titleSmall,
                maxLines = 1
            )
            
            // Quantity and Unit
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}",
                style = Typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    errorMessage: String = ""
) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(errorMessage) }
    
    // Update error when errorMessage changes
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            error = errorMessage
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = {
            Column {
                Text("This will permanently delete your account and all associated data.")
                Text("This action cannot be undone. Please enter your password to confirm.")
                Spacer(modifier = Modifier.height(16.dp))
                
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = ErrorColor,
                        style = Typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        // Clear error when user types
                        if (error.isNotEmpty()) {
                            error = ""
                        }
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (password.isNotEmpty()) {
                        onConfirm(password)
                    } else {
                        error = "Please enter your password"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text("Delete Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    errorMessage: String = ""
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(errorMessage) }
    
    // Update error when errorMessage changes
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            error = errorMessage
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = ErrorColor,
                        style = Typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { 
                        currentPassword = it
                        // Clear error when user types
                        if (error.isNotEmpty()) {
                            error = ""
                        }
                    },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        // Clear error when user types
                        if (error.isNotEmpty()) {
                            error = ""
                        }
                    },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it
                        // Clear error when user types
                        if (error.isNotEmpty()) {
                            error = ""
                        }
                    },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    when {
                        currentPassword.isEmpty() -> {
                            error = "Please enter your current password"
                        }
                        newPassword.isEmpty() -> {
                            error = "Please enter a new password"
                        }
                        newPassword != confirmPassword -> {
                            error = "Both passwords must match"
                        }
                        else -> {
                            onConfirm(currentPassword, newPassword)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditOptionsCard(
    user: User, 
    ingredients: List<UserIngredients>,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    
    // 错误消息状态
    var deleteAccountError by remember { mutableStateOf("") }
    var changePasswordError by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { 
                showDeleteDialog = false
                deleteAccountError = ""
            },
            onConfirm = { password ->
                coroutineScope.launch {
                    isSyncing = true
                    try {
                        val firebaseService = FirebaseService()
                        val result = firebaseService.deleteUserAccount(user.id, password)
                        isSyncing = false
                        
                        if (result) {
                            showDeleteDialog = false
                            snackbarHostState.showSnackbar("Account deleted successfully")
                            userViewModel.logout()
                            navController.navigate("login")
                        } else {
                            // 在对话框内显示错误
                            deleteAccountError = "Password mismatch "
                        }
                    } catch (e: Exception) {
                        isSyncing = false
                        // 在对话框内显示错误
                        deleteAccountError = "Account deletion failed: ${e.message}"
                    }
                }
            },
            errorMessage = deleteAccountError
        )
    }
    
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { 
                showPasswordDialog = false
                changePasswordError = ""
            },
            onConfirm = { currentPassword, newPassword ->
                coroutineScope.launch {
                    isSyncing = true
                    try {
                        val firebaseService = FirebaseService()
                        val result = firebaseService.updateUserPassword(user.id, currentPassword, newPassword)
                        isSyncing = false
                        
                        if (result) {
                            snackbarHostState.showSnackbar("Password updated successfully")
                            showPasswordDialog = false
                        } else {
                            // 在对话框内显示错误
                            changePasswordError = "Current password mismatch"
                        }
                    } catch (e: Exception) {
                        isSyncing = false
                        // 在对话框内显示错误
                        changePasswordError = "Error updating password: ${e.message}"
                    }
                }
            },
            errorMessage = changePasswordError
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Profile Section", style = Typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { 
                    coroutineScope.launch {
                        isSyncing = true
                        try {
                            val firebaseService = FirebaseService()
                            val syncResult = firebaseService.syncUserIngredients(ingredients, user.id)
                            val userSynced = firebaseService.syncUserData(user)
                            isSyncing = false
                            
                            if (syncResult && userSynced) {
                                snackbarHostState.showSnackbar("Data synced successfully")
                            } else {
                                snackbarHostState.showSnackbar("Data sync failed")
                            }
                        } catch (e: Exception) {
                            isSyncing = false
                            snackbarHostState.showSnackbar("Sync Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Sync to Cloud")
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Change Password")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text("Delete Account")
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = InfoColor)
                }
            }

            SnackbarHost(hostState = snackbarHostState)
        }
    }
}
