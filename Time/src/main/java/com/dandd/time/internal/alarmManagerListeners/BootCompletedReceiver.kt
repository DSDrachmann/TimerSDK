package com.dandd.time.internal.alarmManagerListeners

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dandd.time.domain.model.TimerEntity
import com.dandd.time.domain.model.TimerStatus
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
            rescheduleTimers(context)
        }
    }

    private fun rescheduleTimers(context: Context?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //we only want to reschedule does that are marked as "active".
                val allTimers = getTimerEntities(context)?.filter { it.status == TimerStatus.ACTIVE.rawValue }

                if(context != null) //i don't expect the context to be null, but just precaution.
                {
                    allTimers?.forEach { timer ->
                        val epochTimeForAlarmManager = timer.initialDateForSettingTimerInEpoch + (timer.remainingTime*1000)
                        Log.i("BootCompletedReciever", "time for AlarmManager is: $epochTimeForAlarmManager")
                        val pendingIntent = getPendingIntent(context, timer)
                        setTimerInAlarmManagerWhenReschedulingAfterReboot(context, epochTimeForAlarmManager, pendingIntent)
                    }
                }
            } catch (e: Exception) {
                Log.e("BootCompletedReceiver", "Error rescheduling alarms: ${e.message}")
            }
        }
    }

    private suspend fun getTimerEntities(context: Context?): List<TimerEntity>? {
        val database = context?.let { timerDatabaseProvider.getInstance(it) }
            ?.let { TimerDatabaseRepository(it) }
        val allTimers = database?.getAllTimers()?.filter { it.status == TimerStatus.ACTIVE.rawValue }
        return allTimers
    }

    private fun setTimerInAlarmManagerWhenReschedulingAfterReboot(
        context: Context,
        epochTimeForAlarmManager: Long,
        pendingIntent: PendingIntent
    ) {
        //get the alarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //set the alarm using the setExactAndAllowWhileIdle option
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            epochTimeForAlarmManager,
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