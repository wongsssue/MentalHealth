package com.example.mentalhealthemotion.Data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val API_KEY = "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"

interface ChatbotApi {
    @Headers("Authorization: Bearer $API_KEY")
    @POST("v1/chat/completions")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}