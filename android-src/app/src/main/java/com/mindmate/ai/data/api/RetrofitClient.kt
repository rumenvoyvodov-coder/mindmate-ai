package com.mindmate.ai.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.mindmate.ai.data.repository.TokenManager

object RetrofitClient {
    private const val BASE_URL = "https://mindmate-backend-production.up.railway.app/api/v1/"

    fun create(tokenManager: TokenManager): MindMateApi {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MindMateApi::class.java)
    }
}
