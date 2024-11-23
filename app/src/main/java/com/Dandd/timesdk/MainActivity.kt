package com.dandd.timesdk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.dandd.time.domain.TimerProvider
import com.dandd.time.domain.model.TimerStatus
import com.dandd.time.internal.Permissions.PermissionLogic
import com.dandd.time.internal.notificationActivity.NotificationLogic
import com.dandd.timesdk.ui.theme.TimeSDKTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val timerProvider by lazy { TimerProvider(context = this@MainActivity) }
    private val timerFunc by lazy { timerProvider.getTimerFunc() }
    private val timerDatabase by lazy { timerProvider.getTimerDB() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val triggerNotificationPermissionEffect = remember { mutableStateOf(false) }
            val triggerSetAlarmEffect = remember { mutableStateOf(false) }
            val triggerRequestAlarmPermissionEffect = remember { mutableStateOf(false) }
            val triggerPauseTimerEffect = remember { mutableStateOf(false) }
            val triggerReactivateAlarmEffect = remember { mutableStateOf(false) }
            val triggerCancelAlarmEffect = remember { mutableStateOf(false) }
            val triggerDeleteAlarmEffect = remember { mutableStateOf(false) }
            val triggerGetTimersEffect = remember { mutableStateOf(false) }

            TimeSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    Greeting(name = "Android", modifier = Modifier.padding(innerPadding))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Button(onClick = { triggerNotificationPermissionEffect.value = !triggerNotificationPermissionEffect.value }) {
                                Text(text = "Allow notifications")
                            }
                            Button(onClick = { triggerRequestAlarmPermissionEffect.value = !triggerRequestAlarmPermissionEffect.value }) {
                                Text(text = "request alarm permission")
                            }
                            Button(onClick = { triggerSetAlarmEffect.value = !triggerSetAlarmEffect.value }) {
                                Text(text = "Set an alarm")
                            }
                            Button(onClick = { triggerPauseTimerEffect.value = !triggerPauseTimerEffect.value }) {
                                Text(text = "Pause the alarm")
                            }
                            Button(onClick = { triggerReactivateAlarmEffect.value = !triggerReactivateAlarmEffect.value }) {
                                Text(text = "Reactivate the alarm")
                            }
                            Button(onClick = { triggerCancelAlarmEffect.value = !triggerCancelAlarmEffect.value }) {
                                Text(text = "Cancel the alarm")
                            }
                            Button(onClick = { triggerDeleteAlarmEffect.value = !triggerDeleteAlarmEffect.value }) {
                                Text(text = "Delete all alarms")
                            }
                            Button(onClick = { triggerGetTimersEffect.value = !triggerGetTimersEffect.value }) {
                                Text(text = "Get all alarms")
                            }


                            if (triggerNotificationPermissionEffect.value) {
                                LaunchedEffect(triggerNotificationPermissionEffect.value) {
                                    NotificationLogic().checkForPermissions(context = this@MainActivity)
                                    // Additional operations can be performed here as needed
                                }
                            }
                            if (triggerGetTimersEffect.value) {
                                LaunchedEffect(triggerGetTimersEffect.value) {
                                    var result = timerFunc.getTimers("Dan")
                                    print("")
                                }
                            }
                            if (triggerDeleteAlarmEffect.value) {
                                LaunchedEffect(triggerDeleteAlarmEffect.value) {
                                    val timers = timerDatabase.getAllTimers()
                                    timers.forEach {
                                        timerFunc.cancelTimer(this@MainActivity, it)
                                    }

                                    timerDatabase.removeAllTimers()

                                    // Additional operations can be performed here as needed
                                }
                            }
                            if (triggerSetAlarmEffect.value) {
                                LaunchedEffect(triggerSetAlarmEffect.value) {
                                    if(PermissionLogic().checkAlarmPermission(context = this@MainActivity))
                                    {

                                        val currentTimeInStringFormat = setUpCurrentTimeForAlarm()

                                        //val list = timerDatabase.getAllTimers()
                                        //timerDatabase.removeAllTimers()

                                        timerFunc.createTimer(
                                            initialTimerTime = currentTimeInStringFormat,
                                            context = this@MainActivity
                                        )
                                    } else {
                                        Toast.makeText(this@MainActivity, "Request alarm permission", Toast.LENGTH_SHORT).show()
                                    }
                                    // Additional operations can be performed here as needed
                                }
                            }
                            if(triggerRequestAlarmPermissionEffect.value) {
                                LaunchedEffect(triggerRequestAlarmPermissionEffect.value) {
                                    PermissionLogic().requestAlarmPermission(context = this@MainActivity)
                                }
                            }
                            if(triggerPauseTimerEffect.value) {
                                LaunchedEffect(triggerPauseTimerEffect.value) {
                                    val timers = timerDatabase.getAllTimers().filter { it.status == TimerStatus.ACTIVE.rawValue }
                                    timers.forEach {
                                        timerFunc.pauseTimer(context = this@MainActivity, timerEntity = it)
                                    }
                                }
                            }
                            if(triggerReactivateAlarmEffect.value) {
                                LaunchedEffect(triggerReactivateAlarmEffect.value) {
                                    val timers = timerDatabase.getAllTimers().filter { it.status == TimerStatus.PAUSED.rawValue }
                                    timers.forEach {
                                        timerFunc.reActivateTimer(context = this@MainActivity, it)
                                    }
                                }
                            }
                            if(triggerCancelAlarmEffect.value) {
                                LaunchedEffect(triggerCancelAlarmEffect.value) {
                                    val timers = timerDatabase.getAllTimers().filter { it.status != TimerStatus.INACTIVE.rawValue }
                                    timers.forEach {
                                        timerFunc.cancelTimer(context = this@MainActivity, it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        lifecycleScope.launch {
            timerFunc.shutDownOrDestroyOrGoIntoBackground()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            timerFunc.shutDownOrDestroyOrGoIntoBackground()
        }
    }

    override fun onRestart() {
        super.onRestart()
        //Not sure i want to do anything when it comes to this part.
    }

    private fun setUpCurrentTimeForAlarm(): String {
        return "00:00:60"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimeSDKTheme {
        Greeting("Android")
    }
}