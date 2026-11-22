package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedArticleEntity::class,
        FavoriteTeamEntity::class,
        PollVoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SportsDatabase : RoomDatabase() {
    abstract fun sportsDao(): SportsDao

    companion object {
        @Volatile
        private var INSTANCE: SportsDatabase? = null

        fun getDatabase(context: Context): SportsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportsDatabase::class.java,
                    "sports_actu_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
