package com.example.gemniapi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Insert
    suspend fun createSession(session: ChatSession): Long

    @Query("SELECT * FROM chat_sessions WHERE userEmail = :userEmail ORDER BY createdAt DESC")
    fun getUserSessions(userEmail: String): Flow<List<ChatSession>>

    @Delete
    suspend fun deleteSession(session: ChatSession)
}