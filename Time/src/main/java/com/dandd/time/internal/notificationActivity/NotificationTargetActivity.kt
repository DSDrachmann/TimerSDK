package com.dandd.time.internal.notificationActivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Dandd.time.R

class NotificationTargetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to your layout
        setContentView(R.layout.activity_notification_target)

        // You can retrieve extras from the intent if needed
        val timerId = intent.getStringExtra("timerId")
        // Use the timerId or other intent extras to perform specific actions, like displaying a message
    }
}