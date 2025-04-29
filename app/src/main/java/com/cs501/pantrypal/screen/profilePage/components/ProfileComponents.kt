package com.cs501.pantrypal.screen.profilePage.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.firebase.FirebaseService
import com.cs501.pantrypal.ui.theme.*
import com.cs501.pantrypal.util.PasswordCheck
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun MyPantrySection(
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
fun EmptyPantryMessage() {
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
fun IngredientsRow(ingredients: List<UserIngredients>) {
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
fun NotLoggedInContent(onLoginClick: () -> Unit) {
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
    errorMessage: String = "",
    isLoading: Boolean = false
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = {
            Column {
                Text("This will permanently delete your account and all associated data.")
                Text("This action cannot be undone. Please enter your password to confirm.")
                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = ErrorColor,
                        style = Typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.isNotEmpty()) {
                        onConfirm(password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Delete Account")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    errorMessage: String = "",
    isLoading: Boolean = false
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = ErrorColor,
                        style = Typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        currentPassword.isEmpty() -> onConfirm("", "") // 外部处理
                        newPassword.isEmpty() -> onConfirm("", "")
                        newPassword != confirmPassword -> onConfirm("", "")
                        else -> onConfirm(currentPassword, newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditOptionsCard(
    user: User, 
    ingredients: List<UserIngredients>,
    navController: NavController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }

    var isSyncingToCloud by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }

    var deleteAccountError by remember { mutableStateOf("") }
    var changePasswordError by remember { mutableStateOf("") }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = {
                showDeleteDialog = false
                deleteAccountError = ""
                isDeletingAccount = false
            },
            onConfirm = { password ->
                coroutineScope.launch {
                    isDeletingAccount = true
                    try {
                        val passwordCheck = PasswordCheck()
                        val isPasswordCorrect = passwordCheck.verifyPassword(password, user.password)

                        if (isPasswordCorrect) {
                            val firebaseService = FirebaseService()
                            val remoteResult = firebaseService.deleteUserAccount(user.id)
                            val localResult = userViewModel.deleteUserAccount(user)

                            if (remoteResult && localResult) {
                                snackbarHostState.showSnackbar("Account deleted successfully")
                                userViewModel.logout()
                                navController.navigate("login")
                                showDeleteDialog = false
                            } else {
                                deleteAccountError = "Account deletion failed"
                            }
                        } else {
                            deleteAccountError = "Password mismatch"
                        }
                    } catch (e: Exception) {
                        deleteAccountError = "Account deletion failed: ${e.message}"
                    } finally {
                        isDeletingAccount = false
                    }
                }
            },
            errorMessage = deleteAccountError,
            isLoading = isDeletingAccount
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
                    isChangingPassword = true
                    try {
                        val passwordCheck = PasswordCheck()
                        val isPasswordCorrect = passwordCheck.verifyPassword(currentPassword, user.password)
                        if (!isPasswordCorrect){
                            changePasswordError = "Current password mismatch"
                            isChangingPassword = false
                            return@launch
                        }

                        if (newPassword == currentPassword) {
                            changePasswordError = "New password cannot be the same as current password"
                            isChangingPassword = false
                            return@launch
                        }

                        if (!PasswordCheck().validatePassword(newPassword)) {
                            changePasswordError = "New password must be at least 8 characters long"
                            isChangingPassword = false
                            return@launch
                        }

                        val firebaseService = FirebaseService()
                        val newUserData = user.copy(password = passwordCheck.hashPassword(newPassword))
                        val localResult = userViewModel.updateUserProfile(newUserData)
                        val remoteResult = firebaseService.updateUserPassword(user.id, newPassword)
                        isChangingPassword = false

                        if (localResult && remoteResult) {
                            showPasswordDialog = false
                            snackbarHostState.showSnackbar("Password changed successfully")
                        } else {
                            changePasswordError = "Password change failed"
                        }
                    } catch (e: Exception) {
                        isChangingPassword = false
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
                        isSyncingToCloud = true
                        try {
                            val firebaseService = FirebaseService()
                            val ingredientsResult = firebaseService.syncUserIngredients(ingredients, user.id)
                            val userResult = firebaseService.syncUserData(user)
                            val savedRecipes = recipeViewModel.getAllSavedRecipes()
                            val cookbookResult = firebaseService.syncRecipes(savedRecipes, user.id)
                            isSyncingToCloud = false
                            
                            if (userResult && ingredientsResult && cookbookResult) {
                                snackbarHostState.showSnackbar("Data synced successfully")
                            } else {
                                snackbarHostState.showSnackbar("Data sync failed")
                            }
                        } catch (e: Exception) {
                            isSyncingToCloud = false
                            snackbarHostState.showSnackbar("Sync Error: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor),
                enabled = !isSyncingToCloud && !isChangingPassword && !isDeletingAccount
            ) {
                if (isSyncingToCloud) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Sync to Cloud")
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor),
                enabled = !isSyncingToCloud && !isChangingPassword && !isDeletingAccount
            ) {
                Text("Change Password")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                enabled = !isSyncingToCloud && !isChangingPassword && !isDeletingAccount
            ) {
                Text("Delete Account")
            }
            Spacer(modifier = Modifier.height(8.dp))


        }
    }
}