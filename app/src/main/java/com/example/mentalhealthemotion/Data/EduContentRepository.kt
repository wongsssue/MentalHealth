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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 66666,
                contentTitle = "21 Emotion Regulation Worksheets & Strategies",
                contentDescription = "As humans, we will never have complete control over what we feel, but we have a lot more influence over how we feel than you might have heard. The skills that allow you to manage and direct your emotions are called emotion regulation skills (see self-regulation), and it doesn’t take a pilgrimage to a holy site or thousands of dollars to learn these secrets to feeling better. This article will see you learn about emotion regulation and help you develop and improve the skills necessary for staying balanced and emotionally stable.",
                resourceUrl = "https://positivepsychology.com/emotion-regulation-worksheets-strategies-dbt-skills/",
                imageUrl = "https://images.pexels.com/photos/47547/squirrel-animal-cute-rodents-47547.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 33333,
                contentTitle = "11 Comforting Tips for Emotional Regulation",
                contentDescription = "It's hard to know what to do when you feel overwhelmed with an emotion. Do you just accept it, ignore it, or try to stop the feeling? There’s no right or wrong way to deal with feelings, but there may be steps that work better for you. Here’s a little bit about feelings, emotional regulation, and what to do if you’re struggling with both.",
                resourceUrl = "https://www.thecounselingpalette.com/post/emotional-regulation?srsltid=AfmBOoorPRK_OOhyLzYdwzkFoQsr70zZOJDhcKBHb3Z4iC9Mwsh0wp9K",
                imageUrl = "https://images.pexels.com/photos/668353/pexels-photo-668353.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 22222,
                contentTitle = "“Why can’t I control my emotions?” 9 emotional regulation tips",
                contentDescription = "Navigating overwhelming feelings can be challenging. Find out why and how to develop emotional regulation skills for a more balanced life.",
                resourceUrl = "https://www.calm.com/blog/why-cant-i-control-my-emotions",
                imageUrl = "https://images.pexels.com/photos/3772612/pexels-photo-3772612.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11111,
                contentTitle = "Do You Know How to Manage Your Emotions and Why It Matters?",
                contentDescription = "If you notice that balancing and controlling your emotions is challenging, developing emotional regulation skills can help.",
                resourceUrl = "https://psychcentral.com/health/emotional-regulation",
                imageUrl = "https://images.pexels.com/photos/1254140/pexels-photo-1254140.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11100,
                contentTitle = "7 Tips to Improve Your Emotional Regulation in Recovery",
                contentDescription = "The term “emotional regulation” refers to your ability to, according to scientists, “modify the duration or intensity of emotions to best respond to environmental challenges.” As you’ve moved through different stages of addiction treatment and recovery, you might have touched on this concept while exploring the root causes of substance use disorder (SUD) and alcohol use disorder (AUD). Here are some additional emotional regulation techniques you may find helpful.",
                resourceUrl = "https://willingway.com/7-tips-to-improve-your-emotional-regulation-in-recovery/",
                imageUrl = "https://images.pexels.com/photos/7176318/pexels-photo-7176318.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11122,
                contentTitle = "9 Emotional Regulation Tips for Anyone Who’s Struggling Right Now",
                contentDescription = "Developing ways to give time and space to our difficult emotions is especially important right now. Amid the coronavirus pandemic, there are a lot of feelings going around. If you don’t have practice tolerating discomfort and harnessing unwieldy feelings into something manageable, there’s a good chance you’re having a really hard time right now. To help, consider these therapist-approved tips for tackling your emotions head-on.",
                resourceUrl = "https://www.self.com/story/emotional-regulation-skills",
                imageUrl = "https://images.pexels.com/photos/7278847/pexels-photo-7278847.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11133,
                contentTitle = "Emotional Regulation: Practical Tips for Tough Emotions",
                contentDescription = "Everyone struggles to manage their feelings sometimes. Whether it's frustration, sadness, or anxiety, learning emotional regulation helps you handle challenges with confidence. Therapy and mental health coaching can teach us how to regulate emotions through life’s ups and downs.",
                resourceUrl = "https://www.lyrahealth.com/blog/emotional-regulation/",
                imageUrl = "https://images.pexels.com/photos/869258/pexels-photo-869258.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11144,
                contentTitle = "How to Develop and Practice Self-Regulation",
                contentDescription = "Self-regulation is the ability to control one's behavior, emotions, and thoughts in the pursuit of long-term goals.1 More specifically, emotional self-regulation refers to the ability to manage disruptive emotions and impulses—in other words, to think before acting. Self-regulation also involves the ability to rebound from disappointment and to act in a way consistent with your values. It is one of the five key components of emotional intelligence. This article discusses how self-regulation develops and the important impact it can have. It also covers some common problems you may face and what you can do to self-regulate more effectively.",
                resourceUrl = "https://www.verywellmind.com/how-you-can-practice-self-regulation-4163536",
                imageUrl = "https://images.pexels.com/photos/5656742/pexels-photo-5656742.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            ),
            EduContent(
                contentID = 11155,
                contentTitle = "8 Tips on How to Help a Teen Regulate Their Emotions",
                contentDescription = "Emotional regulation is vital for maintaining psychological well-being and navigating the complexities of daily life. This skill enables individuals to modulate their emotional responses in a way that aligns with the demands of different situations. By fostering a balanced approach to regulate their emotions, individuals can effectively cope with stress, manage interpersonal relationships, and make sound decisions. Emotional regulation also plays a crucial role in preventing impulsive reactions and mitigating the impact of negative experiences on mental health. Moreover, it contributes to resilience, allowing individuals to bounce back from setbacks and adapt to changing circumstances. Ultimately, learning how to regulate your emotions is essential for fostering a stable and fulfilling life, promoting mental health, and enhancing overall quality of life.",
                resourceUrl = "https://www.lilaccenter.org/blog/8-tips-on-how-to-help-a-teen-regulate-their-emotions",
                imageUrl = "https://images.pexels.com/photos/8419181/pexels-photo-8419181.jpeg?auto=compress&cs=tinysrgb&h=350",
                dateCreated = generateCurrentDate()
            )
        )

        try {
            if (isOnline()) {
                for (content in defaultContents) {
                    val document =
                        contentCollection.document(content.contentID.toString()).get().await()
                    if (!document.exists()) {
                        contentCollection.document(content.contentID.toString()).set(content)
                            .await()
                        Log.d("EduContentRepository", "Added ${content.contentTitle} to Firestore.")
                    } else {
                        Log.d(
                            "EduContentRepository",
                            "${content.contentTitle} already exists in Firestore, skipping insert."
                        )
                    }
                }
            }

            for (content in defaultContents) {
                val localContent = eduContentDao.getContentById(content.contentID)
                if (localContent == null) {
                    eduContentDao.insertContent(content)
                    Log.d(
                        "EduContentRepository",
                        "Added ${content.contentTitle} to local database."
                    )
                } else {
                    Log.d(
                        "EduContentRepository",
                        "${content.contentTitle} already exists in local database, skipping insert."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("EduContentRepository", "Failed to initialize default content: ${e.message}")
        }
    }

    fun generateCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
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
