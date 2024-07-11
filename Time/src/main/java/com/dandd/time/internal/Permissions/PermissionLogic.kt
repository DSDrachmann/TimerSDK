package com.dandd.time.internal.Permissions

import android.app.Activity.ALARM_SERVICE
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.provider.Settings

class PermissionLogic {

    fun checkAlarmPermission(context: Context): Boolean {
        val alarmManager =
            context.getSystemService(ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun requestAlarmPermission(context: Context) {
        val intent =
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    }
}