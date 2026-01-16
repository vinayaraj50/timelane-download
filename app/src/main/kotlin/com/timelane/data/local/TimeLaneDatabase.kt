package com.timelane.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [EventEntity::class, TaskEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TimeLaneDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
}
