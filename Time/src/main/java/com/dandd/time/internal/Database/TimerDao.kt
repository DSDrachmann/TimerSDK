package com.dandd.time.internal.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dandd.time.domain.model.TimerEntity


//this is your interface, the interface that decides how you interact with the database
//The FavoriteRoomDatabase inherits this
@Dao
internal interface TimerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun setTimer(timer: TimerEntity): Long

    @Delete
    suspend fun removeTimer(timer: TimerEntity): Int

    @Query("UPDATE TimerEntity SET remainingTime = :remainingTime, status = :isActive WHERE timerId = :timerId")
    suspend fun updateTimer(remainingTime: Long, timerId: String, isActive: Int): Int

    @Query("Select * FROM timerEntity")
    suspend fun getAllTimers(): List<TimerEntity>

    @Query("Delete FROM timerEntity")
    suspend fun removeAllTimers(): Int

    @Query("Select * FROM timerEntity WHERE accountName = :accountName")
    suspend fun getTimersOnAccountName(accountName: String): List<TimerEntity>

    @Query("Select * FROM TimerEntity WHERE timerId = :timerId")
    suspend fun getTimerOnTimerId(timerId: String) : TimerEntity

    @Query("Delete FROM timerEntity WHERE accountName = :accountName")
    suspend fun removeAllTimersOnAccountName(accountName: String): Int

    @Query("Delete FROM timerEntity WHERE timerId = :timerId AND accountName = :accountName")
    suspend fun removeTimerOnAccountName(timerId: String, accountName: String): Int
}
