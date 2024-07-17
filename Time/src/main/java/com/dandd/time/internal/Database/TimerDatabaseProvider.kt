package com.dandd.time.internal.Database

import android.content.Context
import androidx.room.Room

//this provides the database, that is, it creates it and returns it.
//this is the first class you create when you want to instantiate the method
internal object timerDatabaseProvider {
    private var instance: TimerRoomDatabase? = null

    fun getInstance(context: Context): TimerRoomDatabase {
        return instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }
    }

    private fun buildDatabase(context: Context): TimerRoomDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TimerRoomDatabase::class.java,
            "timer_clock_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
