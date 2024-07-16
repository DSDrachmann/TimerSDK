package com.dandd.time.domain

import com.dandd.time.domain.model.TimerEntity

interface ITimerDB {
    suspend fun insertTimerEntity(item: TimerEntity)
    suspend fun removeTimer(item: TimerEntity)
    suspend fun updateTimer(item: TimerEntity)
    suspend fun getAllTimers(): List<TimerEntity>
    suspend fun removeAllTimers()
    suspend fun getTimersOnAccountName(accountName: String): List<TimerEntity>
    suspend fun getTimerOnTimerId(timerId: String) : TimerEntity
    suspend fun removeAllTimersOnAccountName(accountName: String)
    suspend fun removeTimerOnAccountName(timerId: String, accountName: String)
}