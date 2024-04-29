package com.example.gemniapi.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChatMessage::class], version = 1)
abstract class ChatMessagesDatabase : RoomDatabase() {
    abstract fun chatMessagesDao(): ChatMessagesDao
}