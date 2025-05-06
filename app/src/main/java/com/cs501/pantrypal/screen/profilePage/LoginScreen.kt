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
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController, snackbarHostState: SnackbarHostState
) {
    val userViewModel: UserViewModel = AppViewModelProvider.userViewModel
    // Check if the user is already logged in
    LaunchedEffect(userViewModel.isUserLoggedIn()) {
        if (userViewModel.isUserLoggedIn()) {
            navController.navigate("discover") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    suspend fun performLogin(
        emailAddress: String,
        password: String,
        userViewModel: UserViewModel,
        navController: NavController,
        snackbarHostState: SnackbarHostState,
        onLoading: (Boolean) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (emailAddress.isBlank() || password.isBlank()) {
            onError("Email and password cannot be empty")
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            onError("Invalid email address")
            return false
        }

        onLoading(true)
        val loginResult = userViewModel.login(emailAddress, password)
        val success = loginResult["success"] as Boolean
        val message = loginResult["message"] as String

        onLoading(false)

        if (!success) {
            onError(message)
            return false
        }

        navController.navigate("discover") {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Long)
            }
            popUpTo("login") { inclusive = true }
        }

        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Login") }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            })
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
                style = Typography.displayMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = emailAddress,
                onValueChange = { emailAddress = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ),
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ErrorColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        performLogin(
                            emailAddress,
                            password,
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
                        modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login")
                }
            }

            TextButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier.padding(vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = InfoColor)
            ) {
                Text("Do not have an account? Create One")
            }
        }
    }
}