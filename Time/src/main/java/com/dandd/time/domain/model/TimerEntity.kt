package com.dandd.time.domain.model

import androidx.room.Entity

@Entity (primaryKeys = ["timerId"])
data class TimerEntity (
    val timerId: String,
    val initialValue: Float,
    val remainingTime: Float,
    val accountName: String?,
    val status: Int = 0
)