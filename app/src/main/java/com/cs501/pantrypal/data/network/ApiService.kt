package com.cs501.pantrypal.data.network


import com.cs501.pantrypal.BuildConfig
import com.cs501.pantrypal.data.model.FoodIdResponse
import com.cs501.pantrypal.data.model.FoodResponse
import com.cs501.pantrypal.data.model.RecipeResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


const val edamamId = BuildConfig.EDAMAM_APP_ID
const val edamamKey = BuildConfig.EDAMAM_APP_KEY

interface ApiService {
    @GET("api/recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") ingredients: String,
        @Query("app_id") appId: String = edamamId,
        @Query("app_key") appKey: String = edamamKey,
        @Query("random") random: Boolean = true,
        @Header("Edamam-Account-User") user: String = "random"
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
