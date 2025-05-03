package com.cs501.pantrypal.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.PrimaryLight
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.ShakeSensorManager
import com.cs501.pantrypal.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun RecipeSearchScreen(viewModel: RecipeViewModel, navController: NavController, snackbarHostState: SnackbarHostState) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    
    var ingredients by remember { mutableStateOf(listOf("")) }
    val maxIngredients = 5
    var showShakeInfo by remember { mutableStateOf(false) }
    
    // Set up shake detection
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shakeSensorManager = remember { ShakeSensorManager(context) }


    
    // Register and unregister the sensor when the composable enters/leaves composition
    DisposableEffect(shakeSensorManager) {
        shakeSensorManager.setOnShakeListener {
            coroutineScope.launch {
                viewModel.getRandomRecipes()
                snackbarHostState.showSnackbar(message = "Shake detected! Finding recipes with your pantry ingredients...", duration = SnackbarDuration.Short)
            }
        }
        shakeSensorManager.register()
        
        onDispose {
            shakeSensorManager.unregister()
        }
    }

    if (isTablet) {
        TabletRecipeSearchLayout(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState,
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            showShakeInfo = showShakeInfo,
            onShowShakeInfoChange = { showShakeInfo = it },
            onIngredientsChange = { ingredients = it }
        )
    } else {
        PhoneRecipeSearchLayout(
            viewModel = viewModel,
            navController = navController,
            snackbarHostState = snackbarHostState,
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            showShakeInfo = showShakeInfo,
            onShowShakeInfoChange = { showShakeInfo = it },
            onIngredientsChange = { ingredients = it }
        )
    }

    if (showShakeInfo) {
        AlertDialog(
            onDismissRequest = { showShakeInfo = false },
            title = { Text("Shake for Surprise") },
            text = {
                Text("Shake your device to get recipe suggestions based on ingredients from your pantry! If your pantry is empty, we'll use trending ingredients instead. Perfect when you're not sure what to cook with what you have on hand.")
            },
            confirmButton = {
                TextButton(onClick = { showShakeInfo = false }) {
                    Text("Got it!")
                }
            }
        )
    }
}

@Composable
fun TabletRecipeSearchLayout(
    viewModel: RecipeViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    ingredients: List<String>,
    maxIngredients: Int,
    showShakeInfo: Boolean,
    onShowShakeInfoChange: (Boolean) -> Unit,
    onIngredientsChange: (List<String>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Row(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ){
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Text("Discover", style = Typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Enter ingredients (max $maxIngredients)",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                }

                IngredientInputSection(
                    ingredients = ingredients,
                    maxIngredients = maxIngredients,
                    onIngredientChange = { index, value ->
                        onIngredientsChange(ingredients.toMutableList().apply {
                            this[index] = value
                        })
                    },
                    onAddIngredient = {
                        if (ingredients.size < maxIngredients) {
                            onIngredientsChange(ingredients + "")
                        }
                    },
                    onRemoveIngredient = { index ->
                        if (ingredients.size > 1) {
                            onIngredientsChange(ingredients.filterIndexed { i, _ -> i != index })
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            val nonEmptyIngredients = ingredients.filter { it.isNotBlank() }
                            if (nonEmptyIngredients.isNotEmpty()) {
                                viewModel.searchRecipes(nonEmptyIngredients.joinToString(", "))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(InfoColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Search")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Button(
                        onClick = { viewModel.getRandomRecipes() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Shake for Pantry Recipes", style = MaterialTheme.typography.labelLarge)
                    }

                    IconButton(
                        onClick = { onShowShakeInfoChange(true) },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Information",
                            tint = InfoColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f)
                .padding(16.dp)
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.recipes) { recipe ->
                        //val isSaved = viewModel.isRecipeSaved(recipe)
                        val formattedCalories = String.format("%.2f", recipe.calories)

                        Card(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedRecipe = recipe
                                    navController.navigate("detail")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = recipe.image,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .padding(end = 8.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = recipe.label,
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(color = Color(0xFF388E3C))) { // ✅ 绿色数字
                                                append(formattedCalories)
                                            }
                                            append(" ")
                                            withStyle(style = SpanStyle(color = Color.Black)) { // ✅ 黑色单位
                                                append("calories")
                                            }
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                    )

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val exists = viewModel.isRecipeInCookbook(recipe.uri, "Default")
                                                if (exists) {
                                                    snackbarHostState.showSnackbar("Already added to Default!")
                                                } else {
                                                    viewModel.saveRecipeToCookbook(recipe, "Default")
                                                    snackbarHostState.showSnackbar("Saved to Default!")
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(30.dp) ,// 控制竖向高度更窄
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer, // 更浅的主题色
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer   // 匹配的文字色
                                        )

                                    ) {
                                        Text("Save", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
//                        Card(
//                            modifier = Modifier
//                                .padding(8.dp)
//                                .fillMaxWidth()
//                                .clickable {
//                                    viewModel.selectedRecipe = recipe
//                                    navController.navigate("detail")
//                                }
//                        ) {
//                            Column {
//                                Text(
//                                    recipe.label,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    modifier = Modifier.padding(8.dp)
//                                )
//                                AsyncImage(
//                                    model = recipe.image,
//                                    contentDescription = null,
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                                Row(modifier = Modifier.padding(8.dp)) {
//
//                                    TextButton(
//                                        onClick = {
//                                            coroutineScope.launch {
//                                                val exists = viewModel.isRecipeInCookbook(recipe.uri, "Default")
//                                                if (exists) {
//                                                    snackbarHostState.showSnackbar("Already added to Default!")
//                                                    //isSavedInDefault = false
//                                                } else {
//                                                    // 如果没保存过，才保存
//                                                    viewModel.saveRecipeToCookbook(recipe, "Default")
//                                                    snackbarHostState.showSnackbar("Saved to Default!")
//                                                }
//                                            }
//                                        }
//                                    ) {
//                                        Text( "Save", style = MaterialTheme.typography.labelMedium)
//                                    }
//
//                                }
//                            }
//                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneRecipeSearchLayout(
    viewModel: RecipeViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    ingredients: List<String>,
    maxIngredients: Int,
    showShakeInfo: Boolean,
    onShowShakeInfoChange: (Boolean) -> Unit,
    onIngredientsChange: (List<String>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()


    Column(modifier = Modifier.padding(16.dp)) {
        Text("Discover Recipes", style = Typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Enter ingredients (max $maxIngredients)", 
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
        }

        IngredientInputSection(
            ingredients = ingredients,
            maxIngredients = maxIngredients,
            onIngredientChange = { index, value -> 
                onIngredientsChange(ingredients.toMutableList().apply {
                    this[index] = value
                })
            },
            onAddIngredient = {
                if (ingredients.size < maxIngredients) {
                    onIngredientsChange(ingredients + "")
                }
            },
            onRemoveIngredient = { index ->
                if (ingredients.size > 1) {
                    onIngredientsChange(ingredients.filterIndexed { i, _ -> i != index })
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    val nonEmptyIngredients = ingredients.filter { it.isNotBlank() }
                    if (nonEmptyIngredients.isNotEmpty()) {
                        viewModel.searchRecipes(nonEmptyIngredients.joinToString(", "))
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(InfoColor,)
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box {
                Button(onClick = { viewModel.getRandomRecipes() }) {
                    Text("Shake for Pantry Recipes", style = MaterialTheme.typography.labelLarge)
                }

                IconButton(
                    onClick = { onShowShakeInfoChange(true) },
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Information",
                        tint = InfoColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {

            LazyColumn {
                items(viewModel.recipes) { recipe ->
                    val coroutineScope = rememberCoroutineScope()
                    val formattedCalories = String.format("%.2f", recipe.calories)


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                viewModel.selectedRecipe = recipe
                                navController.navigate("detail")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = recipe.image,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(end = 8.dp)
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = recipe.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )

//                                Text(
//                                    text = recipe.calories.toString() + " calories",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(bottom = 8.dp),
//                                    color = PrimaryLight
//                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(color = Color(0xFF388E3C))) { // ✅ 绿色数字
                                            append(formattedCalories)
                                        }
                                        append(" ")
                                        withStyle(style = SpanStyle(color = Color.Black)) { // ✅ 黑色单位
                                            append("calories")
                                        }
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                )

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val exists = viewModel.isRecipeInCookbook(recipe.uri, "Default")
                                            if (exists) {
                                                snackbarHostState.showSnackbar("Already added to Default!")
                                            } else {
                                                viewModel.saveRecipeToCookbook(recipe, "Default")
                                                snackbarHostState.showSnackbar("Saved to Default!")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer, // 更浅的主题色
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer   // 匹配的文字色
                                    )

                                ) {
                                    Text("Save", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
//                        Column {
//                            Text(recipe.label, style = MaterialTheme.typography.titleMedium)
//                            AsyncImage(model = recipe.image, contentDescription = null)
//
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                modifier = Modifier.padding(8.dp)
//                            ) {
//                                TextButton(
//                                    onClick = {
//                                        coroutineScope.launch {
//                                            val exists = viewModel.isRecipeInCookbook(recipe.uri, "Default")
//                                            if (exists) {
//                                                snackbarHostState.showSnackbar("Already added to Default!")
//                                                //isSavedInDefault = false
//                                            } else {
//                                                // 如果没保存过，才保存
//                                                viewModel.saveRecipeToCookbook(recipe, "Default")
//                                                snackbarHostState.showSnackbar("Saved to Default!")
//                                            }
//                                        }
//                                    }
//                                ) {
//                                    Text( "Save", style = MaterialTheme.typography.labelMedium)
//                                }
//                            }
//                        }
                    }
                }
            }

        }
    }
}

@Composable
fun IngredientInputSection(
    ingredients: List<String>,
    maxIngredients: Int,
    onIngredientChange: (Int, String) -> Unit,
    onAddIngredient: () -> Unit,
    onRemoveIngredient: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ingredients.forEachIndexed { index, ingredient ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                OutlinedTextField(
                    value = ingredient,
                    onValueChange = { onIngredientChange(index, it) },
                    label = { Text("Ingredient ${index + 1}") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    maxLines = 1,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (ingredients.size > 1) {
                            IconButton(onClick = { onRemoveIngredient(index) }) {
                                Icon(Icons.Default.Remove, contentDescription = "Remove Ingredient")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary
                    )
                )

            }
        }
        
        if (ingredients.size < maxIngredients) {
            TextButton(
                onClick = onAddIngredient,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = InfoColor),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ingredient")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Ingredient")
            }
        }
    }
}

