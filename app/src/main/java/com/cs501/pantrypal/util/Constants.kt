package com.cs501.pantrypal.util

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