package com.dandd.time.domain.model

import androidx.room.Entity

@Entity (primaryKeys = ["timerId"])
data class TimerEntity (
    val timerId: String,
    val initialValue: Long,
    val remainingTime: Long,
    val initialDateForSettingTimerInEpoch: Long,
    val accountName: String?,
    val status: Int = 0
)