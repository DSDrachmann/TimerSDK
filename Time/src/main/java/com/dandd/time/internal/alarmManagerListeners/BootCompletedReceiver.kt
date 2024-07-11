package com.dandd.time.internal.alarmManagerListeners

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dandd.time.domain.model.TimerEntity
import com.dandd.time.internal.Database.TimerDatabaseRepository
import com.dandd.time.internal.Database.timerDatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    /**
     * This function is called when the device reboots. It reschedules all alarms that were set
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED") {
            // Reschedule alarms here
            Log.d("BootCompletedReceiver", "Device rebooted - rescheduling alarms...")
            // Example function call - replace with actual method to reschedule alarms
            //rescheduleAlarms(context)
        }
    }

    private fun rescheduleAlarms(context: Context?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allTimers = getTimerEntities(context)

                allTimers?.forEach { timer ->
                    val pendingIntent = getPendingIntent(context, timer)
                    //force unwrapping the "context is safe", here because the context is not null
                    setAlarmInAlarmManager(context!!, timer, pendingIntent)
                }
            } catch (e: Exception) {
                Log.e("BootCompletedReceiver", "Error rescheduling alarms: ${e.message}")
            }
        }
    }

    private suspend fun getTimerEntities(context: Context?): List<TimerEntity>? {
        val database = context?.let { timerDatabaseProvider.getInstance(it) }
            ?.let { TimerDatabaseRepository(it) }
        val allTimers = database?.getAllTimers()?.filter { it.status == 2 }
        return allTimers
    }

    private fun setAlarmInAlarmManager(
        context: Context,
        timer: TimerEntity,
        pendingIntent: PendingIntent
    ) {
        //get the alarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //set the alarm using the setExactAndAllowWhileIdle option
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            (System.currentTimeMillis() + timer.remainingTime).toLong(),
            pendingIntent
        )
    }

    private fun getPendingIntent(
        context: Context?,
        timer: TimerEntity
    ): PendingIntent {
        val intent = Intent(context, TimerExpiredReceiver::class.java).apply {
            putExtra("timerId", timer.timerId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, timer.timerId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }
}