package com.cs501.pantrypal.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.data.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: Recipe, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.label, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .padding(16.dp)
        ) {
            item {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Description", fontWeight = FontWeight.Bold)
                Text("Ingredients:")
            }

            items(recipe.ingredientLines) { line ->
                Text("• $line")
            }
        }
    }
}


//@Composable
//fun RecipeDetailScreen(recipe: Recipe) {
//    LazyColumn(modifier = Modifier.padding(16.dp)) {
//        item {
//            Text(recipe.label, style = MaterialTheme.typography.headlineMedium)
//            AsyncImage(
//                model = recipe.image,
//                contentDescription = null,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text("Description", fontWeight = FontWeight.Bold)
//            Text("Ingredients:")
//        }
//
//        items(recipe.ingredientLines) { line ->
//            Text("• $line")
//        }
//    }
//}
