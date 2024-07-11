package com.dandd.time.internal.alarmManagerListeners

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.Dandd.time.R

class TimerExpiredReceiver : BroadcastReceiver() {

    companion object {
        const val TIMER_CHANNEL_ID = "10"
        const val TIMER_GROUP_ID = "timerGroup"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        //val packageName = context.packageName
        //val metaData = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
        val actiClassName =  "com.dandd.time.internal.notificationActivity.NotificationTargetActivity"

        val notificationIntent = try {
            Intent(context, actiClassName.let { Class.forName(it) })
        } catch (e: ClassNotFoundException) {
            Log.e("TimerExpiredReceiver", "Failed to find activity class: $actiClassName")
            return
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Timer Expired")
            .setContentText("Your timer has gone off.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setGroup(TIMER_GROUP_ID)
            .setAutoCancel(true)

        val timerId = intent.getStringExtra("timerId") ?: return
        val notificationId = timerId.hashCode()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            /*val permissionIntent = Intent(context, PermissionRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(PermissionRequestActivity.EXTRA_PERMISSIONS, arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
            context.startActivity(permissionIntent)
            */
        } else {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Timer Notification Channel"
        val descriptionText = "Channel for Timer Alarm Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(TIMER_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}