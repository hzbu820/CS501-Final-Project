package com.cs501.pantrypal.data.model

data class FoodResponse (
    var food: List<Food>,
    var foodImages: List<FoodImage>
)

data class Food(
    var foodId: Long,
    var foodName: String,
)

data class FoodImage(
    var images: List<Image>
)

data class Image(
    var imageUrl: String,
    var imageType: String,
)

data class FoodIdResponse(
    var foodId: Long
)