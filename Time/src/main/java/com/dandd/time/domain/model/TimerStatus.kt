package com.dandd.time.domain.model

enum class TimerStatus(val rawValue: Int) {
    ACTIVE(2),
    PAUSED(1),
    INACTIVE(0)
}