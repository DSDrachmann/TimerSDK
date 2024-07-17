package com.dandd.time.domain

import android.content.Context
import com.dandd.time.internal.TimerFunctionality
import com.dandd.time.internal.Database.TimerDatabaseRepository
import com.dandd.time.internal.Database.TimerRoomDatabase
import com.dandd.time.internal.Database.timerDatabaseProvider

class TimerProvider(context: Context) {
    private var database: TimerRoomDatabase = timerDatabaseProvider.getInstance(context)
    private var timerDB: TimerDatabaseRepository = TimerDatabaseRepository(database)

    fun getTimerDB(): ITimerDB {
        return timerDB
    }

    fun getTimerFunc(): ITimer {
        return TimerFunctionality(timerDB)
    }
}