package com.dandd.time.internal

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dandd.time.domain.model.TimerEntity

@Database(entities = [TimerEntity::class], version = 1, exportSchema = false)
abstract class TimerRoomDatabase: RoomDatabase() {
    abstract fun timerItemDao(): TimerDao
}