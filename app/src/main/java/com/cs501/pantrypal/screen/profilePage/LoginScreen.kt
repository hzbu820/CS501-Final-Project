package com.cs501.pantrypal.screen.profilePage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(userViewModel: UserViewModel, navController: NavController, snackbarHostState: SnackbarHostState) {
    // Check if the user is already logged in
    LaunchedEffect(userViewModel.isLoggedIn) {
        if (userViewModel.isLoggedIn) {
            navController.navigate("discover") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()

    suspend fun performLogin(
        username: String,
        password: String,
        userViewModel: UserViewModel,
        navController: NavController,
        snackbarHostState: SnackbarHostState,
        onLoading: (Boolean) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (username.isBlank() || password.isBlank()) {
            onError("Username and password cannot be empty")
            return false
        }

        onLoading(true)
        val success = userViewModel.login(username, password)
        onLoading(false)

        if (!success) {
            onError("Username or password is incorrect")
            return false
        }

        navController.navigate("discover") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = "Login Successful", duration = SnackbarDuration.Long)
            }
            popUpTo("login") { inclusive = true }
        }

        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to PantryPal",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            singleLine = true,
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password,),
            singleLine = true
        )
        
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        Button(
            onClick = {
                coroutineScope.launch {
                    performLogin(username, password, userViewModel, navController, snackbarHostState, { isLoading = it }, { errorMessage = it })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
        
        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Create an account")
        }
    }
    }
}