package com.example.gemniapi.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatMessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatMessage: ChatMessage)

    @Query("Select * FROM chat_messages ORDER BY timeStamp DESC")
    fun getAllChatMessages(): MutableList<ChatMessage>
}