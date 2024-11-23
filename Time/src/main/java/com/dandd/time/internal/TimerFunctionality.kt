package com.dandd.time.internal

import com.dandd.time.domain.model.TimerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import com.dandd.time.domain.ITimer
import com.dandd.time.domain.model.TimerStatus
import com.dandd.time.internal.Database.TimerDatabaseRepository
import com.dandd.time.internal.functionality.AlarmManagerFunctionality.Companion.cancelAlarmInAlarmManager
import com.dandd.time.internal.functionality.AlarmManagerFunctionality.Companion.getPendingIntent
import com.dandd.time.internal.functionality.AlarmManagerFunctionality.Companion.setTimerInAlarmManager
import com.dandd.time.internal.functionality.localTimeFunctionality.Companion.convertHHMMSSToSeconds
import com.dandd.time.internal.functionality.localTimeFunctionality.Companion.getEpochTime
import com.dandd.time.internal.functionality.localTimeFunctionality.Companion.getNewTimeForUpdateField

internal class TimerFunctionality(
    private val timerDatabaseAccess: TimerDatabaseRepository
): ITimer {

    /**
     * This is for UI, it runs the loop
     */
    override fun startLoopCoroutine(coroutineScope: CoroutineScope, loopDelay: Int, oneLoopDoneUnit: () -> Unit) {
        coroutineScope.launch(Dispatchers.Default) {
            while (true) {
                delay(timeMillis = loopDelay.toLong())
                oneLoopDoneUnit()
            }
        }
    }

    /**
     * This is for UI, it cancels the timer.
     */
    override fun cancelLoopCoroutine(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.cancel()
    }

    override suspend fun getTimers(accountName: String): List<TimerEntity> {
        val timers = timerDatabaseAccess.getAllTimers()
/*        var correctTimers: MutableList<TimerEntity> = mutableListOf()
        timers.forEach {
            if (it.status == TimerStatus.ACTIVE.rawValue) {
                val remainingTime = getNewTimeForUpdateField(it)
               val updatedTimerEntity: TimerEntity = it.copy(
                    remainingTime = remainingTime,
                )
                correctTimers.add(updatedTimerEntity)
            }
          else {
                correctTimers.add(it)
            }
        }*/
        return timers
    }

    /**
     * Create a timer with the given initial value.
     * @param initialTimerTime The initial value of the timer in hh:mm:ss format.
     * @return The timer entity created.
     */
    override suspend fun createTimer(initialTimerTime: String, accountName: String?, context: Context) : TimerEntity {

        val epochTime = getEpochTime()

        val timerForDatabase = TimerEntity(
            timerId = generateTimerId(),
            initialValue = convertHHMMSSToSeconds(initialTimerTime),
            remainingTime = convertHHMMSSToSeconds(initialTimerTime),
            initialDateForSettingTimerInEpoch = epochTime,
            accountName = accountName,
            status = TimerStatus.ACTIVE.rawValue
        )

        val epochTimeForAlarmManager = epochTime + (timerForDatabase.initialValue * 1000)

        val pendingIntent = getPendingIntent(context, timerForDatabase)
        setTimerInAlarmManager(context, epochTimeForAlarmManager, pendingIntent)

        timerDatabaseAccess.insertTimerEntity(timerForDatabase)

        return timerForDatabase
    }

    override suspend fun reActivateTimer(context: Context, timerEntity: TimerEntity) {
        val epochTimeForAlarmManager = getEpochTime() + (timerEntity.remainingTime*1000)

        val updatedTimerForDatabase: TimerEntity = timerEntity.copy(
            status = TimerStatus.ACTIVE.rawValue,
            initialDateForSettingTimerInEpoch = getEpochTime()
        )

        val pendingIntent = getPendingIntent(context, timerEntity)
        setTimerInAlarmManager(context, epochTimeForAlarmManager, pendingIntent)

        timerDatabaseAccess.updateTimer(updatedTimerForDatabase)
    }

    override suspend fun pauseTimer(context: Context, timerEntity: TimerEntity) {
        val newRemainingTimeForEntity = getNewTimeForUpdateField(timerEntity)

        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = newRemainingTimeForEntity,
            status = TimerStatus.PAUSED.rawValue
        )

        val pendingIntent = getPendingIntent(context, timerEntity)
        cancelAlarmInAlarmManager(context, pendingIntent)

        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    override suspend fun shutDownOrDestroyOrGoIntoBackground() {
        updateTimers()
    }

    /**
     * This is to be used when either manually cancelling a timer or when the timer expires
     */
    override suspend fun cancelTimer(context: Context, timerEntity: TimerEntity) {
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = timerEntity.initialValue,
            status = TimerStatus.INACTIVE.rawValue
        )

        val pendingIntent = getPendingIntent(context, updatedTimerEntity)
        cancelAlarmInAlarmManager(context, pendingIntent)

        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    override suspend fun deleteTimer(context: Context, timerEntity: TimerEntity) {
        cancelTimer(context, timerEntity)
        timerDatabaseAccess.removeTimer(timerEntity)
    }

    override suspend fun deleteAllTimers(context: Context) {
        val allTimers = timerDatabaseAccess.getAllTimers()
        allTimers.forEach {
            cancelTimer(context, it)
        }
        timerDatabaseAccess.removeAllTimers()
    }

    private suspend fun updateTimers() {
        val activeTimersToBeUpdated = timerDatabaseAccess.getAllTimers().filter { it.status == TimerStatus.ACTIVE.rawValue }
        activeTimersToBeUpdated.forEach {
            val remainingTime = getNewTimeForUpdateField(it)
            val updatedTimerEntity: TimerEntity = it.copy(
                remainingTime = remainingTime,
            )
            timerDatabaseAccess.updateTimer(updatedTimerEntity)
        }
    }

    /**
     * Used internally by  the createTimer method
     */
    private fun generateTimerId(): String {
        return UUID.randomUUID().toString()
    }
}