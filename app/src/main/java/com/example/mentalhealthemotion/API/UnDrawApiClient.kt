package com.example.mentalhealthemotion.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object UnDrawApiClient {
    private const val BASE_URL = "https://undraw.co/api/"

    val service: UnDrawApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnDrawApiService::class.java)
    }
}
