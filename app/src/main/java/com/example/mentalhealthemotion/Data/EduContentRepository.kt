package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

class EduContentRepository(
    private val eduContentDao: EduContentDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val contentCollection = firestore.collection("eduContent")

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    suspend fun initDefaultContent() = withContext(Dispatchers.IO) {
        val defaultContents = listOf(
            EduContent(
                contentID = 44444,
                contentTitle = "How to Become the Boss of Your Emotions",
                contentDescription = "You may be able to regulate your emotions without suppressing or controlling them. This can benefit your relationships, mood, and decision making.",
                resourceUrl = "https://www.healthline.com/health/how-to-control-your-emotions",
                imageUrl = "https://images.pexels.com/photos/1056251/pexels-photo-1056251.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = "2025-02-13 20:50:19"
            ),
            EduContent(
                contentID = 66666,
                contentTitle = "21 Emotion Regulation Worksheets & Strategies",
                contentDescription = "As humans, we will never have complete control over what we feel, but we have a lot more influence over how we feel than you might have heard.\n" +
                        "The skills that allow you to manage and direct your emotions are called emotion regulation skills (see self-regulation), and it doesn’t take a pilgrimage to a holy site or thousands of dollars to learn these secrets to feeling better.\n" +
                        "This article will see you learn about emotion regulation and help you develop and improve the skills necessary for staying balanced and emotionally stable.",
                resourceUrl = "https://positivepsychology.com/emotion-regulation-worksheets-strategies-dbt-skills/",
                imageUrl = "https://images.pexels.com/photos/47547/squirrel-animal-cute-rodents-47547.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = "2025-02-14 21:37:54"
            ),
            EduContent(
                contentID = 33333,
                contentTitle = "11 Comforting Tips for Emotional Regulation",
                contentDescription = "It's hard to know what to do when you feel overwhelmed with an emotion. Do you just accept it, ignore it, or try to stop the feeling?" +"\n" + "There’s no right or wrong way to deal with feelings, but there may be steps that work better for you." +"\n" + "Here’s a little bit about feelings, emotional regulation, and what to do if you’re struggling with both.",
                resourceUrl = "https://www.thecounselingpalette.com/post/emotional-regulation?srsltid=AfmBOoorPRK_OOhyLzYdwzkFoQsr70zZOJDhcKBHb3Z4iC9Mwsh0wp9K",
                imageUrl = "https://images.pexels.com/photos/668353/pexels-photo-668353.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = "2025-02-14 21:38:19"
            ),
            EduContent(
                contentID = 22222,
                contentTitle = "“Why can’t I control my emotions?” 9 emotional regulation tips",
                contentDescription = "Navigating overwhelming feelings can be challenging. Find out why and how to develop emotional regulation skills for a more balanced life.",
                resourceUrl = "https://www.calm.com/blog/why-cant-i-control-my-emotions",
                imageUrl = "https://images.pexels.com/photos/3772612/pexels-photo-3772612.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = "2025-02-14 21:40:52"
            ),
            EduContent(
                contentID = 11111,
                contentTitle = "Do You Know How to Manage Your Emotions and Why It Matters?",
                contentDescription = "If you notice that balancing and controlling your emotions is challenging, developing emotional regulation skills can help.",
                resourceUrl = "https://psychcentral.com/health/emotional-regulation",
                imageUrl = "https://images.pexels.com/photos/3772612/pexels-photo-3772612.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = "2025-02-14 21:47:50"
            )
        )

        try {
            if (isOnline()) {
                for (content in defaultContents) {
                    val document = contentCollection.document(content.contentID.toString()).get().await()
                    if (!document.exists()) {
                        contentCollection.document(content.contentID.toString()).set(content).await()
                        Log.d("EduContentRepository", "Added ${content.contentTitle} to Firestore.")
                    } else {
                        Log.d("EduContentRepository", "${content.contentTitle} already exists in Firestore, skipping insert.")
                    }
                }
            }

            for (content in defaultContents) {
                val localContent = eduContentDao.getContentById(content.contentID)
                if (localContent == null) {
                    eduContentDao.insertContent(content)
                    Log.d("EduContentRepository", "Added ${content.contentTitle} to local database.")
                } else {
                    Log.d("EduContentRepository", "${content.contentTitle} already exists in local database, skipping insert.")
                }
            }
        } catch (e: Exception) {
            Log.e("EduContentRepository", "Failed to initialize default content: ${e.message}")
        }
    }

    suspend fun insertContent(eduContent: EduContent) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                contentCollection.document(eduContent.contentID.toString()).set(eduContent).await()
                Log.d("EduContentRepository", "Edu content added successfully in Firestore.")
            }
            eduContentDao.insertContent(eduContent)
        } catch (e: Exception) {
            Log.e("EduContentRepository", "Failed to add content: ${e.message}")
        }
    }

    // Fetch UnDraw Image
    val okHttpClient = OkHttpClient()

    suspend fun fetchImagesFromPexels(keyword: String): List<String> {
        val apiKey = "8LmhH1MgZYJeBlp6RuxlsXaWQUPAEPkXrpCFOejDh68SnmkQXYjVm9OB"
        val url = "https://api.pexels.com/v1/search?query=$keyword"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", apiKey)  // Send API key in the header
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("Repository", "Fetched images: $responseBody")

                    // Parse JSON response
                    responseBody?.let {
                        val jsonObject = JSONObject(it)
                        val photosArray = jsonObject.getJSONArray("photos")
                        val imageUrls = mutableListOf<String>()

                        for (i in 0 until photosArray.length()) {
                            val photoObject = photosArray.getJSONObject(i)
                            val imageUrl = photoObject.getJSONObject("src").getString("medium")
                            imageUrls.add(imageUrl)
                        }
                        return@withContext imageUrls
                    } ?: emptyList()
                } else {
                    Log.e("Repository", "Failed to fetch images: HTTP ${response.code}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("Repository", "Network error: ${e.message}")
                emptyList()
            }
        }
    }


    suspend fun updateContent(eduContent: EduContent) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                contentCollection.document(eduContent.contentID.toString()).set(eduContent).await()
                Log.d("EduContentRepository", "Content updated successfully in Firestore.")
            }
            eduContentDao.updateContent(eduContent)
        } catch (e: Exception) {
            Log.e("EduContentRepository", "Failed to update content: ${e.message}")
        }
    }

    suspend fun deleteContent(contentID: Int) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                contentCollection.document(contentID.toString()).delete().await()
                Log.d("EduContentRepository", "Content deleted successfully in Firestore.")
            }
            eduContentDao.deleteContent(contentID)
        } catch (e: Exception) {
            Log.e("EduContentRepository", "Failed to delete content: ${e.message}")
        }
    }

    suspend fun getAllContent(): LiveData<List<EduContent>> {
        val liveData = MutableLiveData<List<EduContent>>() // Create LiveData

        withContext(Dispatchers.IO) {
            if (isOnline()) {
                try {
                    val snapshot = contentCollection.get().await()
                    val eduContentList = snapshot.documents.mapNotNull { it.toObject(EduContent::class.java) }
                    liveData.postValue(eduContentList)
                } catch (e: Exception) {
                    Log.e("EduContentRepository", "Failed to fetch from Firestore: ${e.message}")
                    val localData = eduContentDao.getAllContent().value ?: emptyList()
                    liveData.postValue(localData)
                }
            } else {
                val localData = eduContentDao.getAllContent().value ?: emptyList()
                liveData.postValue(localData)
            }
        }

        return liveData
    }


    suspend fun getContentById(contentID: Int): EduContent? {
        return if (isOnline()) {
            try {
                val document = contentCollection.document(contentID.toString()).get().await()
                val eduContent = document.toObject(EduContent::class.java)
                eduContent
            } catch (e: Exception) {
                Log.e("EduContentRepository", "Failed to fetch content from Firestore: ${e.message}")
                eduContentDao.getContentById(contentID)
            }
        } else {
            eduContentDao.getContentById(contentID)
        }
    }
}
