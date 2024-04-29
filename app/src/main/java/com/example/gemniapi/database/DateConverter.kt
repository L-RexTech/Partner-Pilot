package com.example.gemniapi.database

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

class DateConverter {
    @TypeConverter
    fun fromDateToLong(date: Date): Long {
        return date.time
    }
}