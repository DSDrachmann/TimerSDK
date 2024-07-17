package com.dandd.time.internal.functionality

import com.dandd.time.domain.model.TimerEntity
import java.time.LocalDateTime
import java.time.ZoneId

class localTimeFunctionality {

    companion object {
        /**
         * This gets you current epoch time
         */
        internal fun getEpochTime(): Long {
            val now = LocalDateTime.now()
            // Convert the current LocalDateTime to epoch milliseconds
            val epochMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            return epochMillis
        }

        internal fun getNewTimeForUpdateField(timer: TimerEntity): Long {
            val timeInEpoch = getEpochTime()
            //subtracts the timeInEpoch of today from our initial time in Epoch and gets the passed time
            //since the timer was activated
            val timePassedForTimer = timeInEpoch - timer.initialDateForSettingTimerInEpoch
            //subtracts the passed time in seconds from the initial value and gets the remaining time
            //that is left before the timer expires
            val remainingTimeForReal = timer.initialValue - (timePassedForTimer / 1000)
            return remainingTimeForReal
        }

        internal fun convertHHMMSSToSeconds(hhmmss: String): Long {
            val hhmmssList = hhmmss.split(":")
            val hours = hhmmssList[0].toLong()
            val minutes = hhmmssList[1].toLong()
            val seconds = hhmmssList[2].toLong()
            val result = hours * 3600 + minutes * 60 + seconds
            return result
        }
    }
}