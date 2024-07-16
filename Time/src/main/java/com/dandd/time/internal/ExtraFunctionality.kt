package com.dandd.time.internal

import android.app.AlarmManager
import android.app.PendingIntent
import com.dandd.time.domain.model.TimerEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.dandd.time.domain.ITimer
import com.dandd.time.domain.model.TimerStatus
import java.util.Date
import com.dandd.time.internal.alarmManagerListeners.TimerExpiredReceiver
import com.dandd.time.internal.Database.TimerDatabaseRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Timer

internal class ExtraFunctionality(
    private val timerDatabaseAccess: TimerDatabaseRepository
): ITimer {

    /**
     * This is for UI, it runs the loop
     */
    override fun startLoopCoroutine(coroutineScope: CoroutineScope, loopDelay: Int, oneLoopDoneUnit: () -> Unit) {
        coroutineScope.launch(Dispatchers.Default) {
            while (true) {
                delay(timeMillis = loopDelay.toLong())
                oneLoopDoneUnit()
            }
        }
    }

    /**
     * This is for UI, it cancels the timer.
     */
    override fun cancelLoopCoroutine(coroutineScope: CoroutineScope) {
        coroutineScope.coroutineContext.cancel()
    }

    /**
     * Create a timer with the given initial value.
     * @param initialTimerTime The initial value of the timer in hh:mm:ss format.
     * @return The timer entity created.
     */
    override suspend fun createTimer(initialTimerTime: String, accountName: String?, context: Context) : TimerEntity {
        val timerForDatabase = TimerEntity(
            timerId = generateTimerId(),
            initialValue = convertHHMMSSToSeconds(initialTimerTime),
            remainingTime = convertHHMMSSToSeconds(initialTimerTime),
            initialDateForSettingTimerInEpoch = getEpochTime(),
            accountName = accountName,
            status = TimerStatus.ACTIVE.rawValue
        )

        val epochTime = timerForDatabase.initialDateForSettingTimerInEpoch
        val timeForAlarmManager = epochTime + (timerForDatabase.initialValue * 1000)

        val pendingIntent = getPendingIntent(context, timerForDatabase)
        setAlarmInAlarmManager(context, timeForAlarmManager, pendingIntent)
        timerDatabaseAccess.insertTimerEntity(timerForDatabase)
        return timerForDatabase
    }

    override suspend fun reActivateTimer(context: Context, timerEntity: TimerEntity) {

        //get the current time in epoch (maybe i can use this when i also pause a timer.
        val currentEpochTime = getEpochTime()
        val nextEpochTimerForAlarmManager = currentEpochTime + (timerEntity.remainingTime*1000)
        val updatedTimerForDatabase: TimerEntity = timerEntity.copy(
            status = TimerStatus.ACTIVE.rawValue
        )

        val pendingIntent = getPendingIntent(context, timerEntity)
        setAlarmInAlarmManager(context, nextEpochTimerForAlarmManager, pendingIntent)
        timerDatabaseAccess.updateTimer(updatedTimerForDatabase)
    }

    override suspend fun pauseTimer(context: Context, timerEntity: TimerEntity) {
        //cancel the alarm
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context, timerEntity)
        cancelAlarmInAlarmManager(alarmManager, pendingIntent)

        val newRemainingTimeForEntity = getNewTimeForUpdateField(timerEntity)
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = newRemainingTimeForEntity,
            status = TimerStatus.PAUSED.rawValue
        )

        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    private fun setUpCurrentTimeForAlarm(): String {
        val currentTime = Date()
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
        val currentTimeInStringFormat = dateFormat.format(currentTime)
        return currentTimeInStringFormat
    }

    /**
     * This is to be used when either manually cancelling a timer or when the timer expires
     */
    override suspend fun cancelTimer(context: Context, timerEntity: TimerEntity) {
        val updatedTimerEntity: TimerEntity = timerEntity.copy(
            remainingTime = timerEntity.initialValue, status = TimerStatus.INACTIVE.rawValue
        )
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        val pendingIntent = getPendingIntent(context, updatedTimerEntity)

        // Cancel the alarm
        cancelAlarmInAlarmManager(alarmManager, pendingIntent)
        // update the status of the entity in the database.
        timerDatabaseAccess.updateTimer(updatedTimerEntity)
    }

    private fun cancelAlarmInAlarmManager(
        alarmManager: AlarmManager,
        pendingIntent: PendingIntent
    ) {
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Used internally by  the createTimer method
     */
    private fun generateTimerId(): String {
        return UUID.randomUUID().toString()
    }

    suspend fun deleteTimer(context: Context, timerEntity: TimerEntity) {
        cancelTimer(context, timerEntity)
        timerDatabaseAccess.removeTimer(timerEntity)
    }

    suspend fun deleteAllTimers(context: Context) {
        val allTimers = timerDatabaseAccess.getAllTimers()
        allTimers.forEach {
            cancelTimer(context, it)
        }
        timerDatabaseAccess.removeAllTimers()
    }

    private fun setAlarmInAlarmManager(
        context: Context,
        timeForAlarmManager: Long,
        pendingIntent: PendingIntent
    ) {
        //get the alarmManager
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeForAlarmManager,
            pendingIntent
        )
    }

    private fun getPendingIntent(context: Context?, timer: TimerEntity): PendingIntent {
        val intent = Intent(context, TimerExpiredReceiver::class.java).apply {
            putExtra("timerId", timer.timerId) }
        val pendingIntent = PendingIntent.getBroadcast(
            context, timer.timerId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return pendingIntent
    }

    private fun getEpochTime(): Long {
        val now = LocalDateTime.now()
        // Convert the current LocalDateTime to epoch milliseconds
        val epochMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return epochMillis
    }

    private fun convertFloatToEpoch(longTimeToBeConverted: Long): Long {
        // Step 0: Convert the float to HH:mm:ss format
        val totalSeconds = longTimeToBeConverted.toLong()
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        val timeString = String.format(locale = Locale.UK,"%02d:%02d:%02d", hours, minutes, seconds)

        // Step 1: Parse the time string to a LocalTime object
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val localTime = LocalTime.parse(timeString, timeFormatter)

        // Step 2: Combine LocalTime with today's date to get a LocalDateTime object
        val todayDate = LocalDate.now()
        val localDateTime = LocalDateTime.of(todayDate, localTime)

        // Step 3: Convert LocalDateTime to ZonedDateTime using the desired time zone (e.g., Europe/Copenhagen)
        val zoneId = ZoneId.of("Europe/Copenhagen")
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)

        // Step 4: Convert ZonedDateTime to epoch time (milliseconds since Unix epoch)
        val epochMillis = zonedDateTime.toInstant().toEpochMilli()

        return epochMillis
    }

    private fun convertHHMMSSToSeconds(hhmmss: String): Long {
        val hhmmssList = hhmmss.split(":")
        val hours = hhmmssList[0].toLong()
        val minutes = hhmmssList[1].toLong()
        val seconds = hhmmssList[2].toLong()
        val result = hours * 3600 + minutes * 60 + seconds
        return result
    }

    suspend fun shutDownOrDestroy(context: Context) {
        val timers = timerDatabaseAccess.getAllTimers().filter { it.status == TimerStatus.ACTIVE.rawValue }
        timers.forEach {

            //TODO figure out what to do here..
            //do i want to change the remainingTime?
            //what happens on the app being resumed versus the phone
            // being rebooted in terms of updating UI or rescheduling alarms


        }
    }

    private suspend fun updateTimer(context: Context) {
        val activeTimersToBeUpdated = timerDatabaseAccess.getAllTimers().filter { it.status == TimerStatus.ACTIVE.rawValue }
        activeTimersToBeUpdated.forEach {
            val remainingTime = getNewTimeForUpdateField(it)
            val updatedTimerEntity: TimerEntity = it.copy(
                remainingTime = remainingTime,
            )
            timerDatabaseAccess.updateTimer(updatedTimerEntity)
        }
    }

    suspend fun goIntoBackground(context: Context) {

    }

    private fun getNewTimeForUpdateField(timer: TimerEntity): Long {
        //GETS THE CURRENT TIME
        val currentTime = setUpCurrentTimeForAlarm()
        //converts that into seconds
        val currentTimeInSeconds = convertHHMMSSToSeconds(currentTime)
        //converts seconds into epoch based on todays date (so epoch for today)
        val timeinEpoch = convertFloatToEpoch(currentTimeInSeconds)

        //subtracts the timeInEpoch of today from our initial time in Epoch and gets the passed time
        //since the timer was activated
        val timePassedForTimer = timeinEpoch - timer.initialDateForSettingTimerInEpoch

        //subtracts the passed time in seconds from the initial value and gets the remaining time
        //that is left before the timer expires
        val remainingTimeForReal = timer.initialValue - (timePassedForTimer/1000)

        return remainingTimeForReal
    }
}