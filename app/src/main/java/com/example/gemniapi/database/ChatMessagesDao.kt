package com.example.gemniapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessagesDao {
    @Insert
    suspend fun insert(chatMessage: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timeStamp DESC")
    fun getSessionMessages(sessionId: Int): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timeStamp ASC")
    suspend fun getSessionMessagesForExport(sessionId: Int): List<ChatMessage>
}