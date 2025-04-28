package com.cs501.pantrypal.data.network


import com.cs501.pantrypal.data.model.FoodIdResponse
import com.cs501.pantrypal.data.model.FoodResponse
import com.cs501.pantrypal.data.model.RecipeResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query



interface ApiService {
    @GET("api/recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") ingredients: String,
        @Query("app_id") appId: String = "eeddeecf",
        @Query("app_key") appKey: String = "76309a24fdc778fc691885ccb9d2d300",
        @Header("Edamam-Account-User") user: String = "sertarin"
    ): RecipeResponse

    @POST("/rest/server.api")
    suspend fun searchFoodById(
        @Query("method") method: String = "food.get.v4",
        @Query("food_id") foodId: Long,
        @Query("format") format: String = "json"
    ): FoodResponse

    @GET("/rest/food/barcode/find-by-id/v1")
    suspend fun searchIdByCode(
        @Query("barcode") barcode: String,
        @Query("format") format: String = "json"
    ): FoodIdResponse
}
