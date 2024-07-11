package com.dandd.time.internal.notificationActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.dandd.time.internal.alarmManagerListeners.PermissionRequestActivity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat


class NotificationLogic {

    fun checkForPermissions(context: Context) {
        // Check for POST_NOTIFICATIONS permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            val permissionIntent = Intent(context, PermissionRequestActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(PermissionRequestActivity.NOTIFICATION_PERMISSIONS, arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
            context.startActivity(permissionIntent)
        }

        /*
        // Check for SCHEDULE_EXACT_ALARM permission on Android 6.0 (API level 23) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }*/
    }
}