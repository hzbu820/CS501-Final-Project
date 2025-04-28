package com.cs501.pantrypal.screen.profilePage

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.data.firebase.FirebaseService
import com.cs501.pantrypal.screen.profilePage.components.NotLoggedInContent
import com.cs501.pantrypal.screen.profilePage.components.ProfileContent
import com.cs501.pantrypal.screen.profilePage.components.ProfileTopBar
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.viewmodel.UserIngredientsViewModel
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.io.File

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

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (userViewModel.isUserLoggedIn() && currentUser != null) {
            val user = currentUser!!

            var isSyncing by remember { mutableStateOf(false) }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProfileTopBar(userViewModel.isUserLoggedIn()) {
                        userViewModel.logout()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "Logout successful",
                                duration = SnackbarDuration.Long
                            )
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
                                    val ingredientsSynced =
                                        firebaseService.restoreUserIngredients(user.id)

                                    userIngredientsViewModel.updateAllIngredients(ingredientsSynced)

                                    isSyncing = false

                                    if (ingredientsSynced != emptyList<UserIngredients>()) {
                                        snackbarHostState.showSnackbar("Data synced successfully")
                                    } else {
                                        snackbarHostState.showSnackbar("No data to sync")
                                    }
                                } catch (e: Exception) {
                                    isSyncing = false
                                    snackbarHostState.showSnackbar("Sync Error: ${e.message}")
                                }
                            }
                        },
                        navController = navController,
                        userViewModel = userViewModel,
                        snackbarHostState = snackbarHostState
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
            NotLoggedInContent { navController.navigate("login") }
        }
    }

}
