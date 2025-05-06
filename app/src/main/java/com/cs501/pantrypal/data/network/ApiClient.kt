package com.cs501.pantrypal.data.network

import com.cs501.pantrypal.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    val edamamRetrofit: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.edamam.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private const val CONSUMER_KEY = BuildConfig.FATSECRET_KEY
    private const val CONSUMER_SECRET = BuildConfig.FATSECRET_SECRET
    
    val foodRetrofit: ApiService by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(OAuthInterceptor(CONSUMER_KEY, CONSUMER_SECRET))
            .build()
            
        Retrofit.Builder()
            .baseUrl("https://platform.fatsecret.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
