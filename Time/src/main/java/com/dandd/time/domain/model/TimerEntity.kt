package com.dandd.time.domain.model

import androidx.room.Entity

@Entity (primaryKeys = ["timerId", "initialValue", "remainingTime"])
data class TimerEntity (
    val timerId: String,
    //initialValue is for UI
    val initialValue: Float,
    //remainingTime is for UI and for the AlarmManager
    val remainingTime: Float,
    val accountName: String?
)