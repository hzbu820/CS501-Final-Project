package com.cs501.pantrypal.screen

//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.cs501.pantrypal.viewmodel.RecipeViewModel

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
        Text(
            text = "My Cook Books",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "+ Create new cookbook",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable {
                    showDialog = true
                }
        )

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Cookbook") },
                text = {
                    OutlinedTextField(
                        value = newCookbookName,
                        onValueChange = { newCookbookName = it },
                        label = { Text("Cookbook name") }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newCookbookName.isNotBlank()) {
                            viewModel.createCookbook(newCookbookName.trim())
                            newCookbookName = ""
                            showDialog = false
                        }
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        newCookbookName = ""
                        showDialog = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(cookbooks) { cookbookName ->

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
                                .clickable {
                                    viewModel.loadRecipesByCookbook(cookbookName)
                                    navController.navigate("cookbook_detail/${cookbookName}")
                                }
                        ) {
                            Text(
                                text = cookbookName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${recipeCounts[cookbookName]} recipes",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { viewModel.deleteCookbook(cookbookName) }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_delete),
                                contentDescription = "Delete Cookbook"
                            )
                        }
                    }
                }
            }
        }
    }
}