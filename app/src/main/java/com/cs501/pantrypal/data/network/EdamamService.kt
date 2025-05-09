package com.cs501.pantrypal.data.network


import com.cs501.pantrypal.data.model.RecipeResponse
import retrofit2.http.GET
import retrofit2.http.Query



interface EdamamService {
    @GET("api/recipes/v2")
    suspend fun searchRecipes(
        @Query("type") type: String = "public",
        @Query("q") ingredients: String,
        @Query("app_id") appId: String = "e5544b43",
        @Query("app_key") appKey: String = "bdbe86cbc86014789a049104c6ed1759"
    ): RecipeResponse
}
