package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_articles")
data class SavedArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val author: String,
    val date: String,
    val readTimeMinutes: Int,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_teams")
data class FavoriteTeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val sport: String,
    val league: String
)

@Entity(tableName = "poll_votes")
data class PollVoteEntity(
    @PrimaryKey val pollId: String,
    val selectedOptionId: Int
)
