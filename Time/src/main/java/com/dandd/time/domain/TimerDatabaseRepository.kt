package com.dandd.time.domain

import com.dandd.time.domain.model.TimerEntity
import com.dandd.time.internal.TimerRoomDatabase

//this is the class that you interact with that interacts with the object that interacts with the database.
//So first create the favoriteDatabaseProvider and give that database result to this class.

class TimerDatabaseRepository(private val database: TimerRoomDatabase){
    private val timerDao = database.timerItemDao()

    suspend fun insertTimerEntity(item: TimerEntity) {
        try {
            timerDao.setTimer(item)
        } catch (e: Exception) {
            val message = "an insert related error on set a timer happened, see exception: $e, it happened on timerId: ${item.timerId} and initialValue: ${item.initialValue} and remainingTime: ${item.remainingTime}"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun removeTimer(item: TimerEntity) {
        try {
            timerDao.removeTimer(item.timerId)
        } catch (e: Exception) {
            val message = "a remove related error on set a timer happened, see exception: $e, it happened on timerId: ${item.timerId} and initialValue: ${item.initialValue} and remainingTime: ${item.remainingTime}"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun updateTimer(item: TimerEntity) {
        try {
            timerDao.updateTimer(item)
        } catch (e: Exception) {
            val message = "an update related error on set a timer happened, see exception: $e, it happened on timerId: ${item.timerId} and initialValue: ${item.initialValue} and remainingTime: ${item.remainingTime}"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun getAllTimers(): List<TimerEntity> {
        try {
            return timerDao.getAllTimers()
        } catch (e: Exception) {
            val message = "a get all timers failed, see exception: $e"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun removeAllTimers() {
        try {
            timerDao.removeAllTimers()
        } catch (e: Exception) {
            val message = "a remove related error on remove all timers happened, see exception: $e"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun getTimersOnAccountName(accountName: String): List<TimerEntity> {
        try {
            return timerDao.getTimersOnAccountName(accountName)
        } catch (e: Exception) {
            val message = "a get all timers on account name: $accountName failed, see exception: $e"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun removeAllTimersOnAccountName(accountName: String) {
        try {
            timerDao.removeAllTimersOnAccountName(accountName)
        } catch (e: Exception) {
            val message = "a remove related error on remove all timers on account name: $accountName happened, see exception: $e"
            throw DatabaseOperationException(message, e)
        }
    }

    suspend fun removeTimerOnAccountName(timerId: String, accountName: String) {
        try {
            timerDao.removeTimerOnAccountName(timerId, accountName)
        } catch (e: Exception) {
            val message = "a remove related error on remove a timer on account name: $accountName happened, see exception: $e"
            throw DatabaseOperationException(message, e)
        }
    }
}
