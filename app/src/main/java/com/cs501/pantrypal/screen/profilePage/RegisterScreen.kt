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
fun RegisterScreen(userViewModel: UserViewModel, navController: NavController, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(userViewModel.isLoggedIn) {
        if (userViewModel.isLoggedIn) {
            navController.navigate("discover") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun performRegister(
        username: String,
        password: String,
        confirmPassword: String,
        email: String,
        userViewModel: UserViewModel,
        navController: NavController,
        snackbarHostState: SnackbarHostState,
        onLoading: (Boolean) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        // Easy validation
        when {
            username.isBlank() -> {
                onError("Username cannot be empty")
                return false
            }
            password.isBlank() -> {
                onError("Password cannot be empty")
                return false
            }
            password != confirmPassword -> {
                onError("Passwords do not match")
                return false
            }
            password.length < 6 -> {
                onError("Password must be at least 6 characters long")
                return false
            }
            email.isNotBlank() && !isValidEmail(email) -> {
                onError("Please enter a valid email address")
                return false
            }
        }

        onLoading(true)
        val success = userViewModel.register(username, password, email)
        onLoading(false)

        if (!success) {
            onError("Username already exists")
            return false
        }

        navController.navigate("discover") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = "Register Successful", duration = SnackbarDuration.Long)
            }
            popUpTo("register") { inclusive = true }
        }
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
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
                text = "Create an New Account",
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email(Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
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
                        performRegister(
                            username,
                            password,
                            confirmPassword,
                            email,
                            userViewModel,
                            navController,
                            snackbarHostState,
                            { isLoading = it },
                            { errorMessage = it }
                        )
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
                    Text("Register")
                }
            }

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Already have an account? Login")
            }
        }
    }
}

// Simple email validation function
private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}