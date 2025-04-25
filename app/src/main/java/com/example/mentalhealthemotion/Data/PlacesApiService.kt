package com.example.mentalhealthemotion.Data

import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,  // Format: "latitude,longitude"
        @Query("radius") radius: Int = 5000, // 5 km
        @Query("type") type: String, // Dynamically pass place type
        @Query("key") apiKey: String
    ): PlacesResponse
}

