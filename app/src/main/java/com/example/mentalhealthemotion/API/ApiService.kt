package com.example.mentalhealthemotion.API

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("questions")
    suspend fun getQuestions(): List<String>

    @POST("submit")
    suspend fun submitAnswers(@Body answers: List<String?>): String
}