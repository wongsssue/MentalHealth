package com.example.mentalhealthemotion.API

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

class EduService {
    private val client = HttpClient(OkHttp)

    suspend fun fetchEducationalResources(): List<EducationalResource> {
        val response: HttpResponse = client.get("https://your-edu-api-url.com/resources")
        return response.body()
    }
}

@Serializable
data class EducationalResource(val title: String, val content: List<String>)