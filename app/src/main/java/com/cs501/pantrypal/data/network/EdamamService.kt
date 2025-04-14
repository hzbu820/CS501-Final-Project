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
        @Query("app_key") appKey: String = "c1449006ec0d6009a0f969caaf890c60"
    ): RecipeResponse
}
