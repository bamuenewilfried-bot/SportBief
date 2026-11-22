package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SportsDao {
    @Query("SELECT * FROM saved_articles ORDER BY savedAt DESC")
    fun getAllSavedArticles(): Flow<List<SavedArticleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_articles WHERE id = :id)")
    fun isArticleSaved(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticle(article: SavedArticleEntity)

    @Query("DELETE FROM saved_articles WHERE id = :id")
    suspend fun deleteSavedArticle(id: String)

    @Query("SELECT * FROM favorite_teams ORDER BY name ASC")
    fun getFavoriteTeams(): Flow<List<FavoriteTeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteTeam(team: FavoriteTeamEntity)

    @Query("DELETE FROM favorite_teams WHERE id = :id")
    suspend fun removeFavoriteTeam(id: String)

    @Query("SELECT * FROM poll_votes")
    fun getAllPollVotes(): Flow<List<PollVoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordPollVote(vote: PollVoteEntity)
}
