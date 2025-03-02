package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PSQIRepository(private val dao: PSQIDAO, private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val resultCollection = firestore.collection("psqiResults")

    private fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getPSQIQuestions(): List<PSQIQuestion> {
        return listOf(
            PSQIQuestion(
                1,
                "During the past month, what time have you usually gone to bed at night? (e.g., \"10:30 PM\", \"06:15 AM\").",
                QuestionType.Subjective
            ),
            PSQIQuestion(
                2,
                "How long (in minutes) has it usually taken you to fall asleep each night?",
                QuestionType.Subjective
            ),
            PSQIQuestion(
                3,
                "During the past month, what time have you usually gotten up in the morning? (e.g., \"10:30 PM\", \"06:15 AM\").",
                QuestionType.Subjective
            ),
            PSQIQuestion(
                4,
                "During the past month, how many hours of actual sleep did you get at night? (This may be different from the number of hours you spent in bed.)",
                QuestionType.Subjective
            ),
            PSQIQuestion(
                5,
                "During the past month, how often have you had trouble sleeping because you cannot get to sleep within 30 minutes?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                6,
                "During the past month, how often have you had trouble sleeping because you wake up in the middle of the night or early morning?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                7,
                "During the past month, how often have you had trouble sleeping because you have to get up to use the bathroom?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                8,
                "During the past month, how often have you had trouble sleeping because you cannot breath comfortably?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                9,
                "During the past month, how often have you had trouble sleeping because you cough or snore loudly?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                10,
                "During the past month, how often have you had trouble sleeping because you feel too cold?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                11,
                "During the past month, how often have you had trouble sleeping because you feel too hot?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                12,
                "During the past month, how often have you had trouble sleeping because you had bad dreams?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                13,
                "During the past month, how often have you had trouble sleeping because you have pain?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                14,
                "During the past month, how would you rate your sleep quality overall?",
                QuestionType.Objective(
                    listOf("Very Good", "Fairly Good", "Fairly Bad", "Very Bad")
                )
            ),
            PSQIQuestion(
                15,
                "During the past month, how often have you taken medicine to help you sleep (prescribed or \"over the counter\")?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                16,
                "During the past month, how often have you had trouble staying awake while driving, eating meals or engaging in social activity?",
                QuestionType.Objective(
                    listOf(
                        "Not during the past month",
                        "Less than once a week",
                        "Once or twice a week",
                        "Three or more times a week"
                    )
                )
            ),
            PSQIQuestion(
                17,
                "During the past month, how much of a problem has it been for you to keep up enough enthusiasm to get things done?",
                QuestionType.Objective(
                    listOf(
                        "No problem at all",
                        "Only a very slight problem",
                        "Somewhat of a problem",
                        "A very big problem"
                    )
                )
            )
        )
    }


    suspend fun insertResult(userId: Int, result: PSQIResult) = withContext(Dispatchers.IO){
        try{
            if(isOnline()){
                resultCollection
                    .document(userId.toString())
                    .collection("psqiResults")
                    .document(result.resultID.toString())
                    .set(result)
                    .await()
                Log.d("PSQIRepository", "Result has been save under user $userId in Firestore.")
            }
            dao.insertResult(result)
        }catch (e:Exception){
            Log.e("PSQIRepository", "Result failed to save : ${e.message}")
        }
    }

    suspend fun deleteResult(userId: Int, result: PSQIResult) = withContext(Dispatchers.IO){
        try {
            if (isOnline()) {
                val documentRef = resultCollection
                    .document(userId.toString()) // Reference the user document
                    .collection("psqiResults")  // Reference the mood entries subcollection
                    .document(result.resultID.toString()) // Reference the specific mood entry

                documentRef.delete().await()
                Log.d("PSQIRepository", "Result has been deleted from Firestore for user $userId.")
            }
            dao.deleteResult(result)
            Log.d("PSQIRepository", "Result deleted from Room for user $userId.")
        } catch (e: Exception) {
            Log.e("PSQIRepository", "Failed to delete result for user $userId: ${e.message}")
        }
    }

    suspend fun getAllResults(userId: Int): List<PSQIResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isOnline()) {
                val snapshot = resultCollection
                    .document(userId.toString())
                    .collection("psqiResults")
                    .get()
                    .await()
                val psqiResults = snapshot.documents.mapNotNull { it.toObject(PSQIResult::class.java) }
                psqiResults
            } else {
                dao.getAllResults(userId) // No need for an extra variable
            }
        } catch (e: Exception) {
            Log.e("PSQIRepository", "Failed to retrieve results: ${e.message}")
            emptyList()
        }
    }

    private fun getCurrentMonthYear(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return dateFormat.format(Date())
    }

    fun getDailySleepScoresForMonth(userId: Int): Flow<List<DailySleepScore>> {
        val monthYear = getCurrentMonthYear()

        return flow {
            try {
                if (isOnline()) {
                    // Fetch from Firestore
                    val snapshot = resultCollection
                        .document(userId.toString())
                        .collection("psqiResults")
                        .get()
                        .await()

                    val sleepEntries = snapshot.documents
                        .mapNotNull { it.toObject(PSQIResult::class.java) }
                        .filter { it.date.orEmpty().contains(monthYear) } // Filter by current month-year

                    // Group by day and calculate average score
                    val dailySleepScores = sleepEntries
                        .groupBy { it.date?.substring(0, 2)?.toIntOrNull() ?: 0 } // Group by day
                        .map { (day, scores) ->
                            DailySleepScore(
                                day = day,
                                sleepScore = scores.map { it.score }.average().toFloat()
                            )
                        }
                        .sortedBy { it.day } // Sort by day

                    emit(dailySleepScores) // Emit Firestore data
                } else {
                    // Fetch from Room Database (which already computes avg)
                    val offlineEntries = dao.getDailySleepFeedback(userId, monthYear)
                    emit(offlineEntries) // Emit Room database data
                }
            } catch (e: Exception) {
                Log.e("SleepRepository", "Failed to fetch data: ${e.message}")
                emit(emptyList()) // Emit empty list in case of error
            }
        }.flowOn(Dispatchers.IO) // Run on background thread
    }


}