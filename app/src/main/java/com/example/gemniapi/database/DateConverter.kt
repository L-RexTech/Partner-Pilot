package com.example.gemniapi.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}