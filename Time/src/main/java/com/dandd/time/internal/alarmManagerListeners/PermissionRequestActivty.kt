package com.dandd.time.internal.alarmManagerListeners

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.provider.Settings
import android.widget.Toast

class PermissionRequestActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 2
        const val NOTIFICATION_PERMISSIONS = "extraPermissions"
        private const val EXACT_ALARM_PERMISSION_CODE = 100
        private const val NOTIFICATION_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = intent.getStringArrayExtra(NOTIFICATION_PERMISSIONS)
        if (permissions != null) {
            showRationaleDialog(permissions)
        } else {
            finish() // Finish the activity if no permissions are provided
        }
    }

    private fun showRationaleDialog(permissions: Array<String>) {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs these permissions to function properly. Please grant them.")
            .setPositiveButton("Continue") { dialog, which ->
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
                finish()
            }
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == EXACT_ALARM_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Schedule Exact alarm Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Schedule Exact alarm Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actionsForDeniedPermissions(permissions: Array<out String>) {
        permissions.forEach { permission ->
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Some permissions were denied permanently. You can enable them in app settings.")
                    .setPositiveButton("App Settings") { dialog, which ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
                    .create()
                    .show()
                return
            }
        }
    }
}
