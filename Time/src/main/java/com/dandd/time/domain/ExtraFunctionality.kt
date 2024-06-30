package com.dandd.time.domain

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
import android.content.Intent

class ExtraFunctionality {


    /**
     * This is for UI, it runs the loop
     */
    fun startLoopCoroutine(coroutineScope: CoroutineScope, loopDelay: Int, oneLoopDoneUnit: () -> Unit) {
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
    fun cancelLoopCoroutine(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.cancel()
    }

    /**
     * Used internally bu the createTimer method
     */
    private fun generateTimerId(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Create a timer with the given initial value.
     * It is the apps responsibility to also input this into the app database.
     * @param initialTimerTime The initial value of the timer in hh:mm:ss format.
     * @return The timer entity created.
     */
    fun createTimer(initialTimerTime: String, accountName: String? = null, context: Context) : TimerEntity {
        var timer = TimerEntity(
            timerId = generateTimerId(),
            initialValue = convertHHMMSSToSeconds(initialTimerTime),
            remainingTime = convertHHMMSSToSeconds(initialTimerTime),
            accountName = accountName
        )
        setTimerInAlarmManager(context, timer)
        return timer
    }

    private fun convertHHMMSSToSeconds(hhmmss: String): Float {
        val hhmmssList = hhmmss.split(":")
        val hours = hhmmssList[0].toFloat()
        val minutes = hhmmssList[1].toFloat()
        val seconds = hhmmssList[2].toFloat()
        return hours * 3600 + minutes * 60 + seconds
    }

    fun getRemainingTimeInHHMMSS(initialTimerTime: Float, currentTimerTime: String): String {
        val hours = (initialTimerTime / 3600).toInt()
        val minutes = ((initialTimerTime % 3600) / 60).toInt()
        val seconds = (initialTimerTime % 60).toInt()
        return "$hours:$minutes:$seconds"
    }

    /**
     * This is to be used when creating a timer
     */
    private fun setTimerInAlarmManager(context: Context, timerEntity: TimerEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerExpiredReceiver::class.java)
        intent.putExtra("timerId", timerEntity.timerId)
        val pendingIntent = PendingIntent.getBroadcast(context, timerEntity.timerId.hashCode(), intent, PendingIntent.FLAG_MUTABLE)

        // Schedule the alarm to go off when the timer is supposed to end
        val triggerTime = System.currentTimeMillis() + timerEntity.remainingTime * 1000
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            triggerTime.toLong(), pendingIntent)
    }

    /**
     * This is to be used when either manually cancelling a timer or when the timer expires
     */
    fun cancelAlarm(context: Context, timerEntity: TimerEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerExpiredReceiver::class.java)
        intent.putExtra("timerId", timerEntity.timerId)
        val pendingIntent = PendingIntent.getBroadcast(context, timerEntity.timerId.hashCode(), intent, PendingIntent.FLAG_MUTABLE)

        // Cancel the alarm
        alarmManager.cancel(pendingIntent)
    }
}