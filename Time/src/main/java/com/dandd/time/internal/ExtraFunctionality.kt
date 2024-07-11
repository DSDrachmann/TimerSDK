package com.dandd.time.internal

import android.app.AlarmManager
import android.app.PendingIntent
import com.dandd.time.domain.model.TimerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.dandd.time.domain.ITimer
import com.dandd.time.domain.model.TimerStatus
import java.util.Date
import com.dandd.time.internal.alarmManagerListeners.TimerExpiredReceiver
import com.dandd.time.internal.Database.TimerDatabaseRepository
import java.text.SimpleDateFormat
import java.util.Locale

internal class ExtraFunctionality(
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

    /**
     * Create a timer with the given initial value.
     * @param initialTimerTime The initial value of the timer in hh:mm:ss format.
     * @return The timer entity created.
     */
    override suspend fun createTimer(initialTimerTime: String, accountName: String?, context: Context) : TimerEntity {
        val timer = TimerEntity(
            timerId = generateTimerId(),
            initialValue = convertHHMMSSToSeconds(initialTimerTime),
            remainingTime = convertHHMMSSToSeconds(initialTimerTime),
            accountName = accountName,
            status = TimerStatus.ACTIVE.rawValue
        )

        val pendingIntent = getPendingIntent(context, timer)
        setAlarmInAlarmManager(context, timer, pendingIntent)
        timerDatabaseAccess.insertTimerEntity(timer)
        return timer
    }

    override suspend fun reActivateTimer(context: Context, timerEntity: TimerEntity) {
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            status = TimerStatus.ACTIVE.rawValue
        )
        val pendingIntent = getPendingIntent(context, updatedTimerEntity)
        setAlarmInAlarmManager(context, updatedTimerEntity, pendingIntent)
        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    override suspend fun pauseTimer(context: Context, timerEntity: TimerEntity) {
        //cancel the alarm
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context, timerEntity)
        cancelAlarmInAlarmManager(alarmManager, pendingIntent)

        val currentTime = setUpCurrentTimeForAlarm()
        val remainingTime = timerEntity.remainingTime - convertHHMMSSToSeconds(currentTime)
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = remainingTime,
            status = TimerStatus.PAUSED.rawValue
        )

        // remove the alarm from the database
        timerDatabaseAccess.updateTimer(updatedTimerEntity)
        print("")
    }

    private fun setUpCurrentTimeForAlarm(): String {
        val currentTime = Date()
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val currentTimeInStringFormat = dateFormat.format(currentTime)
        return currentTimeInStringFormat
    }

    /**
     * This is to be used when either manually cancelling a timer or when the timer expires
     */
    override suspend fun cancelTimer(context: Context, timerEntity: TimerEntity) {
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = timerEntity.initialValue, status = TimerStatus.INACTIVE.rawValue
        )
        // val updatedTimerEntity = timerEntity.copy(remainingTime = timerEntity.initialValue, isActive = 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = getPendingIntent(context, updatedTimerEntity)

        // Cancel the alarm
        cancelAlarmInAlarmManager(alarmManager, pendingIntent)
        // remove the alarm from the database
        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    private fun cancelAlarmInAlarmManager(
        alarmManager: AlarmManager,
        pendingIntent: PendingIntent
    ) {
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Used internally by  the createTimer method
     */
    private fun generateTimerId(): String {
        return UUID.randomUUID().toString()
    }


    private fun setAlarmInAlarmManager(
        context: Context,
        timer: TimerEntity,
        pendingIntent: PendingIntent
    ) {
        //get the alarmManager
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        val time =(System.currentTimeMillis() + timer.remainingTime).toLong()

        //set the alarm using the setExactAndAllowWhileIdle option
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    private fun getPendingIntent(context: Context?, timer: TimerEntity): PendingIntent {
        val intent = Intent(context, TimerExpiredReceiver::class.java).apply {
            putExtra("timerId", timer.timerId) }
        val pendingIntent = PendingIntent.getBroadcast(
            context, timer.timerId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent
    }

    private fun convertHHMMSSToSeconds(hhmmss: String): Float {
        val hhmmssList = hhmmss.split(":")
        val hours = hhmmssList[0].toFloat()
        val minutes = hhmmssList[1].toFloat()
        val seconds = hhmmssList[2].toFloat()
        return hours * 3600 + minutes * 60 + seconds
    }
}