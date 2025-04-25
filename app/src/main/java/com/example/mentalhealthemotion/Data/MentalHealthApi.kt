package com.example.mentalhealthemotion.Data

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class PredictionRequest(val text: String)
data class PredictionResponse(val prediction: String)

interface MentalHealthAPI {
    @POST("/predict")
    fun predict(@Body request: PredictionRequest): Call<PredictionResponse>
}
private const val BASE_URL = "http://192.168.100.13:8000"


val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)  // Replace with your server IP
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val api = retrofit.create(MentalHealthAPI::class.java)
