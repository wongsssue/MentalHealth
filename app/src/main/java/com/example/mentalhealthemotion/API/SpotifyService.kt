package com.example.mentalhealthemotion.API

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SpotifyService {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchRecommendedTracks(mood: String, accessToken: String): List<Track> {
        val response: HttpResponse = client.get("https://api.spotify.com/v1/recommendations") {
            headers {
                append("Authorization", "Bearer $accessToken")
            }
            parameter("seed_genres", mood)
            parameter("limit", 5)
        }
        return response.body<SpotifyResponse>().tracks
    }
}

@Serializable
data class SpotifyResponse(val tracks: List<Track>)

@Serializable
data class Track(val name: String, val artists: List<Artist>, val preview_url: String?)

@Serializable
data class Artist(val name: String)