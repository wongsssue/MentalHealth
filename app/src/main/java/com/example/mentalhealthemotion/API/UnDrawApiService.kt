package com.example.mentalhealthemotion.API

import retrofit2.http.GET
import retrofit2.http.Query

interface UnDrawApiService {
    @GET("illustrations")
    suspend fun getIllustrations(@Query("search") keyword: String): UnDrawResponse
}

data class UnDrawResponse(val illustrations: List<Illustration>)
data class Illustration(val image: String, val title: String)
