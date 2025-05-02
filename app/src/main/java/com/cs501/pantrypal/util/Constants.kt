package com.cs501.pantrypal.util

import com.cs501.pantrypal.R

/**
 * Constants used throughout the application
 * Contains shared values like measurement units and food categories
 */
object Constants {
    /**
     * Common measurement units for ingredients and grocery items
     */
    val MEASUREMENT_UNITS = listOf("g", "kg", "ml", "L", "oz", "lb", "gal", "pcs")

    /**
     * Common food categories for organizing ingredients and grocery items
     */
    val FOOD_CATEGORIES = listOf(
        "Fruits", 
        "Vegetables", 
        "Meat", 
        "Dairy", 
        "Grains",
        "Seafood", 
        "Spices", 
        "Beverages", 
        "Snacks", 
        "Other"
    )

    /**
     * Common ingredient image based on the food category
     */
    val FOOD_IMAGES = mapOf(
        "Fruits" to R.drawable.fruits,
        "Vegetables" to R.drawable.vegetable,
        "Meat" to  R.drawable.meat,
        "Dairy" to  R.drawable.dairy,
        "Grains" to   R.drawable.bread,
        "Seafood" to  R.drawable.seafood,
        "Spices" to  R.drawable.spices,
        "Beverages" to  R.drawable.beverages,
        "Snacks" to  R.drawable.snack,
        "Other" to R.drawable.grocery,
        "List" to R.drawable.list
    )

    /**
     * Common Cookbook categories for organizing recipes
     */
    val COOKBOOK_CATEGORIES = listOf(
        "Default",
        "Breakfast",
        "Lunch",
        "Dinner",
        "Dessert",
        "Snacks",
        "Appetizers",
        "Salads",
        "Soups",
    )
}