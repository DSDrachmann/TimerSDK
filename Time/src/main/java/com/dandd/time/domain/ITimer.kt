package com.dandd.time.domain

import android.content.Context
import com.dandd.time.domain.model.TimerEntity
import com.dandd.time.domain.model.TimerStatus
import kotlinx.coroutines.CoroutineScope

interface ITimer {
    /**
     * This is for UI, it runs the loop
     */
    fun startLoopCoroutine(coroutineScope: CoroutineScope, loopDelay: Int, oneLoopDoneUnit: () -> Unit)

    /**
     * This is for UI, it cancels the timer.
     */
    fun cancelLoopCoroutine(coroutineScope: CoroutineScope)

    /**
     * Create a timer with the given initial value.
     * @param initialTimerTime The initial value of the timer in hh:mm:ss format.
     * @return The timer entity created.
     */
    suspend fun createTimer(initialTimerTime: String, accountName: String? = null, context: Context) : TimerEntity

    /**
     * This is to be used when either manually cancelling a timer or when the timer expires
     */
    suspend fun cancelTimer(context: Context, timerEntity: TimerEntity)

    /**
     * This is to be used when the timer is paused.
     */
    suspend fun pauseTimer(context: Context, timerEntity: TimerEntity)

    suspend fun reActivateTimer(context: Context, timerEntity: TimerEntity)
}