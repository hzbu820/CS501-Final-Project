package com.cs501.pantrypal.screen.profilePage

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import java.util.Locale

@SuppressLint("SimpleDateFormat")
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
                val file = java.io.File(fileDir, fileName)
                
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
    onNavigateToAddIngredient: () -> Unit
) {
    // User Info Section
    UserInfoSection(user, profileImageUri, onToggleEditOptions, onSelectImage)

    // Edit User Info Section
    if (showEditOptions) {
        EditOptionsCard()
    }

    // User Pantry Section
    MyPantrySection(ingredients, onNavigateToAddIngredient)
}

@Composable
private fun UserInfoSection(
    user: User,
    profileImageUri: Uri?,
    onEditClick: () -> Unit,
    onImageClick: () -> Unit
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
                    val imageFile = java.io.File(user.profileImageUrl)
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

        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = "Edit Profile",
                tint = InfoColor
            )
        }
    }
}

@Composable
private fun EditOptionsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Profile Options", style = Typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: Implement change password */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Change Password")
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { /* TODO: Implement change email */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor)
            ) {
                Text("Change Email")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: Implement delete account */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
            ) {
                Text("Delete Account")
            }
            Spacer(modifier = Modifier.height(8.dp))
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
    LazyRow {
        items(ingredients) { ingredient ->
            IngredientItem(ingredient)
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
