package com.dandd.time.internal.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dandd.time.domain.model.TimerEntity

@Database(entities = [TimerEntity::class], version = 3, exportSchema = false)
internal abstract class TimerRoomDatabase: RoomDatabase() {
    abstract fun timerItemDao(): TimerDao
}