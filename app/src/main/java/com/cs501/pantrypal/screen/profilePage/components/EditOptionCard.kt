package com.cs501.pantrypal.screen.profilePage.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.firebase.FirebaseService
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography

import com.cs501.pantrypal.util.PasswordCheck
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun EditOptionsCard(
    ingredients: List<UserIngredients>,
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
        }, onConfirm = { password ->
            coroutineScope.launch {
                isDeletingAccount = true
                val result = handleDeleteAccount(password, userViewModel)
                val success = result["success"] as Boolean
                val message = result["message"] as String
                if (success) {
                    showDeleteDialog = false
                    snackbarHostState.showSnackbar(message)
                } else {
                    deleteAccountError = message
                }
                isDeletingAccount = false
            }
        }, errorMessage = deleteAccountError, isLoading = isDeletingAccount
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = {
            showPasswordDialog = false
            changePasswordError = ""
        }, onConfirm = { currentPassword, newPassword ->
            coroutineScope.launch {
                isChangingPassword = true
                val result = handlePasswordChange(currentPassword, newPassword, userViewModel)
                val success = result["success"] as Boolean
                val message = result["message"] as String
                isChangingPassword = false
                if (success) {
                    showPasswordDialog = false
                    snackbarHostState.showSnackbar(message)
                } else {
                    changePasswordError = message
                }
            }
        }, errorMessage = changePasswordError
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Edit Profile Section", style = Typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        isSyncingToCloud = true
                        val result = handleSyncToCloud(ingredients, userViewModel, recipeViewModel)
                        val success = result["success"] as Boolean
                        val message = result["message"] as String
                        isSyncingToCloud = false
                        if (success) {
                            snackbarHostState.showSnackbar(message)
                        } else {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = InfoColor),
                enabled = !isSyncingToCloud && !isChangingPassword && !isDeletingAccount
            ) {
                if (isSyncingToCloud) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)
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

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    errorMessage: String = "",
    isLoading: Boolean = false
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Delete Account") }, text = {
        Column {
            Text("This will permanently delete your account and all associated data.")
            Text("This action cannot be undone. Please enter your password to confirm.")
            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ErrorColor,
//                        style = Typography.bodySmall
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
    }, confirmButton = {
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
                    color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Delete Account")
            }
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss, enabled = !isLoading) {
            Text("Cancel")
        }
    })
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    errorMessage: String = "",
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Change Password") }, text = {
        Column {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage, color = ErrorColor, style = Typography.bodySmall
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
    }, confirmButton = {
        Button(
            onClick = {
                isLoading = true
                onConfirm(currentPassword, newPassword)
                isLoading = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = InfoColor),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Confirm")
            }
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss, enabled = !isLoading) {
            Text("Cancel")
        }
    })
}


suspend fun handlePasswordChange(
    currentPassword: String, newPassword: String, userViewModel: UserViewModel
): Map<String, Any> {
    try {
        val storedPassword = userViewModel.currentUser.value?.password
        val passwordCheck = PasswordCheck()
        val isPasswordValid = passwordCheck.verifyPassword(currentPassword, storedPassword!!)

        if (!isPasswordValid) {
            return mapOf("success" to false, "message" to "Current password is incorrect")
        }

        if (newPassword == currentPassword) {
            return mapOf(
                "success" to false,
                "message" to "New password cannot be the same as current password"
            )
        }

        if (!passwordCheck.validatePassword(newPassword)) {
            return mapOf(
                "success" to false,
                "message" to "New password does not meet complexity requirements"
            )
        }

        val firebaseService = FirebaseService.getInstance()
        val hashedPassword = passwordCheck.hashPassword(newPassword)
        val newData = userViewModel.currentUser.value?.copy(password = hashedPassword)
        val localResult = userViewModel.updateUserProfile(newData!!)
        val remoteResult = firebaseService.updateUserPassword(newData.id, hashedPassword)

        if (!localResult && !remoteResult) {
            return mapOf(
                "success" to false,
                "message" to "Password change failed: Unable to update password in local and remote databases"
            )
        } else if (!localResult) {
            return mapOf(
                "success" to false,
                "message" to "Password change failed: Unable to update password in local database"
            )
        } else if (!remoteResult) {
            return mapOf(
                "success" to false,
                "message" to "Password change failed: Unable to update password in remote database"
            )
        }
    } catch (e: Exception) {
        return mapOf("success" to false, "message" to "Password change failed: ${e.message}")
    }
    return mapOf("success" to true, "message" to "Password changed successfully")
}

suspend fun handleDeleteAccount(password: String, userViewModel: UserViewModel): Map<String, Any> {
    val passwordCheck = PasswordCheck()
    val isPasswordValid =
        passwordCheck.verifyPassword(password, userViewModel.currentUser.value?.password!!)

    if (!isPasswordValid) {
        return mapOf("success" to false, "message" to "Password is incorrect")
    }

    val user = userViewModel.currentUser.value!!
    val firebaseService = FirebaseService.getInstance()
    val remoteResult = firebaseService.deleteUserAccount(user.id)
    val localResult = userViewModel.deleteUserAccount(user)

    if (!remoteResult && !localResult) {
        return mapOf(
            "success" to false,
            "message" to "Account deletion failed: Unable to delete account in local and remote databases"
        )
    } else if (!remoteResult) {
        return mapOf(
            "success" to false,
            "message" to "Account deletion failed: Unable to delete account in remote database"
        )
    } else if (!localResult) {
        return mapOf(
            "success" to false,
            "message" to "Account deletion failed: Unable to delete account in local database"
        )
    }

    return mapOf(
        "success" to true, "message" to "Account deleted successfully"
    )
}

suspend fun handleSyncToCloud(
    ingredients: List<UserIngredients>,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel
): Map<String, Any> {
    val user = userViewModel.currentUser.value!!
    val firebaseService = FirebaseService.getInstance()
    val ingredientSyncResult = firebaseService.syncUserIngredients(ingredients, user.id)
    val savedRecipes = recipeViewModel.getAllSavedRecipes()
    val cookbookResult = firebaseService.syncRecipes(savedRecipes, user.id)
    val userResult = firebaseService.syncUserData(user)
    var message = ""

    message += if (userResult) {
        "User data synced successfully.\r\n"
    } else {
        "User data sync failed.\r\n"
    }

    message += if (ingredientSyncResult) {
        "Ingredients synced successfully.\r\n"
    } else {
        "Ingredients sync failed.\r\n"
    }

    message += if (cookbookResult) {
        "Cookbook synced successfully."
    } else {
        "Cookbook sync failed."
    }

    return mapOf(
        "success" to (userResult && ingredientSyncResult && cookbookResult),
        "message" to message
    )


}