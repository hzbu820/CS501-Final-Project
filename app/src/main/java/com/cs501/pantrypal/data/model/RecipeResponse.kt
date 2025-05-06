package com.cs501.pantrypal.data.model


data class RecipeResponse(
    val hits: List<Hit>
)

data class Hit(
    val recipe: Recipe
)

data class Recipe(
    val label: String,
    val image: String,
    val ingredientLines: List<String>,
    val uri: String,
    val calories: Double,
    val yield: Double,
    val totalTime: Double,
    val cuisineType: List<String>,
)
