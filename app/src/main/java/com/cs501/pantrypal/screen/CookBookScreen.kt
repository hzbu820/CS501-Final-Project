package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cs501.pantrypal.ui.theme.ErrorColor
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.TextSecondary
import com.cs501.pantrypal.ui.theme.TextSecondaryLight
import com.cs501.pantrypal.ui.theme.TextTertiary
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.viewmodel.RecipeViewModel

@Composable
fun CookBookScreen(
    navController: NavController,
    viewModel: RecipeViewModel
) {
    val cookbooks by viewModel.cookbooks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newCookbookName by remember { mutableStateOf("") }
    val recipeCounts by viewModel.cookbookRecipeCounts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCookbooks()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Header
        CookBookScreenHeader()

        Box(modifier = Modifier.fillMaxSize()) {
            if (cookbooks.isEmpty()) {
                EmptyCookBookState()
            } else {
                CookBookList(cookbooks, recipeCounts, navController, viewModel)
            }

            // Floating Action Button to add a new cookbook
            AddCookBookButton(Modifier.align(Alignment.BottomEnd)) { showDialog = true }
        }
        
        // Add Cookbook Dialog
        if (showDialog) {
            AddCookBookDialog(
                newCookbookName = newCookbookName,
                onNameChange = { newCookbookName = it },
                onDismiss = {
                    newCookbookName = ""
                    showDialog = false
                },
                onConfirm = {
                    if (newCookbookName.isNotBlank()) {
                        viewModel.createCookbook(newCookbookName.trim())
                        newCookbookName = ""
                        showDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun CookBookScreenHeader() {
    Text(
        text = "My Cook Books",
        style = Typography.displayLarge,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

@Composable
fun EmptyCookBookState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your cookbook is empty",
            style = Typography.displayMedium,
            color = TextSecondaryLight
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Add a new cookbook to get started",
            style = Typography.bodyMedium,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CookBookList(
    cookbooks: List<String>,
    recipeCounts: Map<String, Int>,
    navController: NavController,
    viewModel: RecipeViewModel
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(cookbooks) { cookbookName ->
            CookBookItem(
                cookbookName = cookbookName,
                recipeCount = recipeCounts[cookbookName] ?: 0,
                onItemClick = {
                    viewModel.loadRecipesByCookbook(cookbookName)
                    navController.navigate("cookbook_detail/${cookbookName}")
                },
                onDeleteClick = { viewModel.deleteCookbook(cookbookName) }
            )
        }
    }
}

@Composable
fun CookBookItem(
    cookbookName: String,
    recipeCount: Int,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onItemClick)
            ) {
                Text(
                    text = cookbookName,
                    style = Typography.titleLarge,
                    color = TextSecondary
                )
                Text(
                    text = "$recipeCount recipes",
                    style = Typography.bodySmall,
                    color = TextTertiary,
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Cookbook",
                    tint = ErrorColor
                )
            }
        }
    }
}

@Composable
fun AddCookBookButton(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier
            .padding(16.dp),
        containerColor = InfoColor,
        shape = CircleShape,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Item",
            tint = Color.White
        )
    }
}

@Composable
fun AddCookBookDialog(
    newCookbookName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Cookbook") },
        text = {
            OutlinedTextField(
                value = newCookbookName,
                onValueChange = onNameChange,
                label = { Text("Cookbook name") }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}