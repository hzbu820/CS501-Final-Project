package com.cs501.pantrypal.data.model

data class FoodResponse(
    val food: Food, val food_images: FoodImages? = null
)

data class Food(
    val food_id: String, val food_name: String

)

data class FoodImages(
    val food_image: List<FoodImage>
)

data class FoodImage(
    val image_url: String, val image_type: String
)

data class FoodIdValue(
    var value: String
)

data class FoodIdResponse(
    var food_id: FoodIdValue
)