package com.example.mentalhealthemotion.Data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MoodEntryRepository(
    private val moodEntryDao: MoodEntryDao,
    private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val moodEntriesCollection = firestore.collection("moodEntries")

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun insertMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                moodEntriesCollection
                    .document(userId.toString())  // Store under user document
                    .collection("moodEntries")
                    .document(moodEntry.moodEntryID.toString())
                    .set(moodEntry)
                    .await()
                Log.d("MoodEntryRepository", "Mood entry saved under user $userId in Firestore.")
            }
            moodEntryDao.insertMoodEntry(moodEntry)  // Save locally with Room
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to save mood entry: ${e.message}")
        }
    }

    suspend fun updateMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val documentRef = moodEntriesCollection
                    .document(userId.toString()) // Reference the user document
                    .collection("moodEntries")  // Subcollection for mood entries
                    .document(moodEntry.moodEntryID.toString()) // Reference the specific mood entry

                documentRef.set(moodEntry).await()
                Log.d("MoodEntryRepository", "Mood entry updated in Firestore for user $userId.")
            }
            moodEntryDao.updateMoodEntry(moodEntry)
            Log.d("MoodEntryRepository", "Mood entry updated in Room for user $userId.")
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to update mood entry for user $userId: ${e.message}")
        }
    }

    suspend fun deleteMoodEntry(userId: Int, moodEntry: MoodEntry) = withContext(Dispatchers.IO) {
        try {
            if (isOnline()) {
                val documentRef = moodEntriesCollection
                    .document(userId.toString()) // Reference the user document
                    .collection("moodEntries")  // Reference the mood entries subcollection
                    .document(moodEntry.moodEntryID.toString()) // Reference the specific mood entry

                documentRef.delete().await()
                Log.d("MoodEntryRepository", "Mood entry deleted from Firestore for user $userId.")
            }
            moodEntryDao.deleteMoodEntry(moodEntry)
            Log.d("MoodEntryRepository", "Mood entry deleted from Room for user $userId.")
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to delete mood entry for user $userId: ${e.message}")
        }
    }


    suspend fun loadMoodEntries(userId: Int): List<MoodEntry> {
        return try {
            if (isOnline()) {
                val snapshot = moodEntriesCollection
                    .document(userId.toString())
                    .collection("moodEntries")
                    .get()
                    .await()
                val moodEntries = snapshot.documents.mapNotNull { it.toObject(MoodEntry::class.java) }
                Log.d("MoodEntryRepository", "Fetched ${moodEntries.size} mood entries from Firestore.")
                moodEntries
            } else {
                val localEntries = moodEntryDao.getAllMoodEntriesByUser(userId)
                Log.d("MoodEntryRepository", "Fetched ${localEntries.size} mood entries from Room (offline).")
                localEntries
            }
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Failed to load mood entries: ${e.message}")
            emptyList()
        }
    }


    // Function to count moods for the current month
    suspend fun countMoodsForCurrentMonth(userId: Int): List<MoodCount> {
        return withContext(Dispatchers.IO) {
            try {
                if (isOnline()) {
                    val currentMonthYear = getCurrentMonthYear()
                    Log.d("MoodEntryRepository", "Current Month-Year: $currentMonthYear")

                    val snapshot = moodEntriesCollection
                        .document(userId.toString())
                        .collection("moodEntries")
                        .get()
                        .await()

                    val filteredMoods = snapshot.documents
                        .mapNotNull { it.toObject(MoodEntry::class.java) }
                        .filter { it.date?.substring(3, 10) == currentMonthYear }
                        .groupingBy { it.moodType }
                        .eachCount()

                    val allMoodCounts = MoodType.values().map { moodType ->
                        MoodCount(moodType.name, filteredMoods[moodType] ?: 0)
                    }

                    Log.d("MoodEntryRepository", "Fetched ${allMoodCounts.size} mood counts from Firestore.")
                    return@withContext allMoodCounts
                } else {
                    throw Exception("Offline mode")
                }
            } catch (e: Exception) {
                Log.e("MoodEntryRepository", "Firestore fetch failed: ${e.message}, falling back to Room.")

                val currentMonthYear = getCurrentMonthYear()
                val localCounts = moodEntryDao.countMoodsByMonthYear(userId, currentMonthYear)

                val allMoodCounts = MoodType.values().map { moodType ->
                    MoodCount(moodType.name, localCounts.find { it.moodType == moodType.name }?.count ?: 0)
                }

                Log.d("MoodEntryRepository", "Fetched ${allMoodCounts.size} mood counts from Room (offline).")
                return@withContext allMoodCounts
            }
        }
    }

    private fun getCurrentMonthYear(): String {
        val timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        return dateFormat.format(Date())
    }


    fun getMoodActivityDataForMonth(userId: Int): Flow<Map<MoodType, List<ActivityPercentage>>> {
        val monthYear = getCurrentMonthYear()

        return flow {
            try {
                if (isOnline()) {
                    // Fetch from Firestore
                    val snapshot = moodEntriesCollection
                        .document(userId.toString())
                        .collection("moodEntries")
                        .get()
                        .await()

                    val moodEntries = snapshot.documents
                        .mapNotNull { it.toObject(MoodEntry::class.java) }
                        .filter { it.date.orEmpty().contains(monthYear) } // Filter by current month-year

                    val moodDataMap = moodEntries.groupBy { it.moodType }.mapValues { (_, entries) ->
                        val totalMoodDays = entries.size.toDouble()
                        val activityCounts = entries.flatMap { it.activityName }
                            .groupingBy { it }
                            .eachCount()

                        activityCounts.map { (activity, count) ->
                            ActivityPercentage(activity, (count / totalMoodDays) * 100)
                        }
                    }

                    emit(moodDataMap) // Emit Firestore data
                } else {
                    // Fetch from Room Database (Must be collected)
                    moodEntryDao.getActivitiesForMoodInMonth(userId, monthYear)
                        .collect { offlineEntries ->
                            val moodDataMap = offlineEntries.groupBy { it.moodType }.mapValues { (_, entries) ->
                                val totalMoodDays = entries.size.toDouble()
                                val activityCounts = entries.flatMap { it.activityName }
                                    .groupingBy { it }
                                    .eachCount()

                                activityCounts.map { (activity, count) ->
                                    ActivityPercentage(activity, (count / totalMoodDays) * 100)
                                }
                            }
                            emit(moodDataMap) // Emit Room database data
                        }
                }
            } catch (e: Exception) {
                Log.e("MoodRepository", "Failed to fetch data: ${e.message}")
                emit(emptyMap()) // Emit empty map in case of error
            }
        }.flowOn(Dispatchers.IO) // Run on background thread
    }

    fun getCurrentWeekDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))

        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        val startOfWeek = dateFormat.format(calendar.time)

        // Move to Sunday
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = dateFormat.format(calendar.time)

        return startOfWeek to endOfWeek
    }

    fun getMoodsForCurrentWeek(userId: Int): Flow<Map<MoodType, List<MoodCountByDay>>> = flow {
        val (startDate, endDate) = getCurrentWeekDateRange() // Get Monday-Sunday range

        try {
            if (isOnline()) {
                val snapshot = moodEntriesCollection
                    .document(userId.toString())
                    .collection("moodEntries")
                    .get()
                    .await()

                // Extract data from Firestore and filter by date
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                val dayFormatter = SimpleDateFormat("dd", Locale.getDefault()) // Extracts day as "01", "02", etc.

                val filteredMoods = snapshot.documents
                    .mapNotNull { it.toObject(MoodEntry::class.java) }
                    .filter { moodEntry ->
                        moodEntry.date?.let { dateStr ->
                            try {
                                val parsedDate = formatter.parse(dateStr) ?: return@filter false
                                val startDateParsed = formatter.parse("$startDate 00:00:00")
                                val endDateParsed = formatter.parse("$endDate 23:59:59.999")

                                parsedDate in startDateParsed..endDateParsed
                            } catch (e: Exception) {
                                false // If parsing fails, exclude this entry
                            }
                        } ?: false // If date is null, exclude it
                    }
                    .groupBy { moodEntry ->
                        moodEntry.date?.let { dateStr -> dayFormatter.format(formatter.parse(dateStr)!!) } ?: "Unknown"
                    }

                val firestoreData = filteredMoods.flatMap { (day, moods) ->
                    moods.groupingBy { it.moodType }.eachCount().map { (moodType, count) ->
                        MoodCountByDay(day, moodType ?: MoodType.meh, count)
                    }
                }.groupBy { it.moodType } // Ensure the grouping is by MoodType

                Log.d("MoodEntryRepository", "Fetched weekly mood counts from Firestore.")
                emit(firestoreData)
                return@flow
            } else {
                throw Exception("Offline mode")
            }
        } catch (e: Exception) {
            Log.e("MoodEntryRepository", "Firestore fetch failed: ${e.message}, falling back to Room.")
        }

        // Fetch from Room database (Offline Mode)
        val weeklyCounts = moodEntryDao.countMoodsByWeek(userId, startDate, endDate)
            .map { moodList ->
                moodList.groupBy { moodEntry ->
                    moodEntry.moodType ?: MoodType.meh
                }.mapValues { (_, counts) ->
                    counts.map { MoodCountByDay(it.day, it.moodType, it.mood_count) } // Use correct `day` field
                }
            }

        Log.d("MoodEntryRepository", "Fetched weekly mood counts from Room (offline).")
        emitAll(weeklyCounts)
    }.flowOn(Dispatchers.IO)

}