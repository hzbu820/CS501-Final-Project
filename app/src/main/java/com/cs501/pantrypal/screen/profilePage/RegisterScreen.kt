package com.cs501.pantrypal.screen.profilePage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cs501.pantrypal.AppViewModelProvider
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.PasswordCheck
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController, snackbarHostState: SnackbarHostState
) {
    val userViewModel: UserViewModel = AppViewModelProvider.userViewModel
    LaunchedEffect(userViewModel.isUserLoggedIn()) {
        if (userViewModel.isUserLoggedIn()) {
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

            !PasswordCheck().validatePassword(password) -> {
                onError("Password must be at least 8 characters long")
                return false
            }

            !email.isNotBlank() -> {
                onError("Email cannot be empty")
                return false
            }

            !isValidEmail(email) -> {
                onError("Invalid email address")
                return false
            }
        }

        onLoading(true)
        val success = userViewModel.register(username, password, email)
        onLoading(false)

        if (!success) {
            onError("Email already exists")
            return false
        }

        navController.navigate("discover") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Register Successful", duration = SnackbarDuration.Long
                )
            }
            popUpTo("register") { inclusive = true }
        }
        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Register") }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            })
        }) { padding ->
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
                style = Typography.displayMedium,
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
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
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
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
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
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
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
                            { errorMessage = it })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(InfoColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), color = InfoColor
                    )
                } else {
                    Text("Register")
                }
            }

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.padding(vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = InfoColor)
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