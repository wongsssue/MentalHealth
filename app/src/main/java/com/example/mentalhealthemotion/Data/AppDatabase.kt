package com.example.mentalhealthemotion.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, MoodEntry::class, EduContent::class, PSQIResult::class, EmergencyContact::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class, MoodTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val moodEntryDao: MoodEntryDao
    abstract val eduContentDao: EduContentDao
    abstract val psqiDao: PSQIDAO
    abstract val emergencyContactDao: EmergencyContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"

                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON;") // Enable Foreign Key Constraints
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}