package com.dandd.time.internal.functionality

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.dandd.time.domain.model.TimerEntity
import com.dandd.time.internal.alarmManagerListeners.TimerExpiredReceiver

class AlarmManagerFunctionality {

    companion object {

        internal fun setTimerInAlarmManager(
            context: Context,
            timeForAlarmManager: Long,
            pendingIntent: PendingIntent
        ) {
            //get the alarmManager
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeForAlarmManager,
                pendingIntent
            )
        }

        internal fun cancelAlarmInAlarmManager(
            context: Context,
            pendingIntent: PendingIntent
        ) {
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(pendingIntent)
        }



        /**
         * Call this before setTimerInAlarmManager
         */
        internal fun getPendingIntent(context: Context?, timer: TimerEntity): PendingIntent {
            val intent = Intent(context, TimerExpiredReceiver::class.java).apply {
                putExtra("timerId", timer.timerId) }
            val pendingIntent = PendingIntent.getBroadcast(
                context, timer.timerId.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            return pendingIntent
        }
    }
}