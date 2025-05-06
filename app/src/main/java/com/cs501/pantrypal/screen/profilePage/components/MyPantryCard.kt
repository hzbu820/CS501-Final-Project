package com.cs501.pantrypal.screen.profilePage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cs501.pantrypal.R
import com.cs501.pantrypal.data.database.UserIngredients
import com.cs501.pantrypal.ui.theme.BackgroundLight
import com.cs501.pantrypal.ui.theme.InfoColor
import com.cs501.pantrypal.ui.theme.Typography
import com.cs501.pantrypal.util.Constants.FOOD_IMAGES

@Composable
fun MyPantryCard(
    ingredients: List<UserIngredients>,
    onAddIngredient: () -> Unit,
    onIngredientClick: (ingredient: UserIngredients) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Pantry", style = Typography.titleLarge
                )

                IconButton(onClick = onAddIngredient) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Ingredient",
                        tint = InfoColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (ingredients.isEmpty()) {
                EmptyPantryMessage()
            } else {
                val isTablet = LocalConfiguration.current.screenWidthDp >= 600
                if (isTablet) {
                    TabletIngredientsLayout(ingredients, onIngredientClick)
                } else {
                    IngredientsRow(ingredients, onIngredientClick)
                }
            }
        }
    }
}

@Composable
fun TabletIngredientsLayout(
    ingredients: List<UserIngredients>, onIngredientClick: (ingredient: UserIngredients) -> Unit
) {
    val groupedIngredients = ingredients.groupBy { it.foodCategory }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        groupedIngredients.forEach { (category, categoryIngredients) ->
            Text(
                text = category,
                style = Typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            LazyRow(
                modifier = Modifier
            ) {
                items(categoryIngredients) { ingredient ->
                    IngredientItem(ingredient, onIngredientClick)
                }
            }

        }
    }
}


@Composable
fun EmptyPantryMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No ingredients in your pantry yet", style = Typography.titleSmall
        )
    }
}

@Composable
fun IngredientsRow(
    ingredients: List<UserIngredients>, onIngredientClick: (ingredient: UserIngredients) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(bottom = 16.dp),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ingredients.size) { index ->
            IngredientItem(ingredients[index], onIngredientClick)
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: UserIngredients, onIngredientClick: (ingredient: UserIngredients) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(enabled = true) {
                onIngredientClick(ingredient)
            },
    ) {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ingredient.image != "") {
                AsyncImage(
                    model = ingredient.image,
                    contentDescription = ingredient.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundLight)
                        .padding(8.dp),
                    error = painterResource(id = R.drawable.grocery),
                    placeholder = painterResource(id = R.drawable.grocery)
                )
            } else {
                Icon(
                    painter = painterResource(
                        id = FOOD_IMAGES[ingredient.foodCategory] ?: R.drawable.grocery
                    ),
                    contentDescription = ingredient.foodCategory,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundLight)
                        .padding(8.dp),
                    tint = Color.Unspecified
                )
            }


            Spacer(modifier = Modifier.height(4.dp))

            // Ingredient Name
            Text(
                text = ingredient.name, style = Typography.titleSmall, maxLines = 1
            )

            // Quantity and Unit
            Text(
                text = "${ingredient.quantity} ${ingredient.unit}",
                style = Typography.bodySmall,
                maxLines = 1
            )
        }
    }
}
