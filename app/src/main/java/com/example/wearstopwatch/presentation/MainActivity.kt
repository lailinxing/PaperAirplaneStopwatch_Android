/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.wearstopwatch.presentation

//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material.icons.filled.Stop

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.TriggerEvent
import android.hardware.TriggerEventListener
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text


//import androidx.wear.compose.material.Icon

class MainActivity : ComponentActivity()
    , SensorEventListener
{
    private lateinit var sensorManager: SensorManager
    private var mFullWakeLock: WakeLock? = null

    private val sides:MutableState<Float> = mutableStateOf(0f);
    private val upDown:MutableState<Float> = mutableStateOf(0f);
    private val gz:MutableState<Float> = mutableStateOf(0f);
    private val gzMax:MutableState<Float> = mutableStateOf(0f);

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var viewModel = viewModel<StopWatchViewModel>()
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()

            StopWatch(
                state = timerState,
                text = stopWatchText,
//                onReset = viewModel::resetTimer,
                onRestart=viewModel::restart,
                onStop= {this::stopCount},
//                viewModel={viewModel},
                modifier = Modifier.fillMaxSize(),
                sides = sides.value,
                upDown = upDown.value,
                gz = gz.value,
                gzMax = gzMax.value
            )


        }

        setUpSensorStuff()
        setUpPowerMng()
//        startAccessibilitySetting()
//        ignoreBattery()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//
        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//
        mSensor?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        val triggerEventListener = object : TriggerEventListener() {
            override fun onTrigger(event: TriggerEvent?) {
                // Do work
            }
        }
        mSensor?.also { sensor ->
            sensorManager.requestTriggerSensor(triggerEventListener, sensor)
        }
    }

//    fun Context.startForegroundService() {
//        Intent(this, ForegroundService::class.java).also { intent ->
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                this.startForegroundService(intent)
//            } else {
//                this.startService(intent)
//            }
//        }
//    }

//    fun ignoreBattery() {
//        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
//        intent.data = Uri.parse("package:$packageName")
//        startActivityForResult(intent, 1)
//    }


    fun startAccessibilitySetting() {
        runCatching {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private  fun setUpPowerMng() {

        val pm = getSystemService(POWER_SERVICE) as PowerManager
//        val wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, );

//        val wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WatchFaceAlarmWokenUp")


//        mFullWakeLock = pm.newWakeLock(
//            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
//            "WatchFaceAlarmWokenUp"
//        )
//        mFullWakeLock.acquire(2000l)

        val wakeLock: PowerManager.WakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            sides.value = event!!.values[0]
            upDown.value  = event!!.values[1]
            gz.value  = event!!.values[2]

            if(gz.value > gzMax.value){
                gzMax.value  = gz.value;
            }
//            println("x: ${event!!.values[0].toString()}");
//            println("y: ${event!!.values[1].toString()}");
//            println("z: ${event!!.values[2].toString()}");
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    private fun stopCount(){

    }
}

@Composable
private fun StopWatch(
    state: TimerState,
    text: String,
//    onReset: () -> Unit,
    onRestart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    sides: Float,
    upDown: Float,
    gz: Float,
    gzMax: Float,
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
//        Button(onClick = onRestart){
//            Text(text = "Start")
//        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){



            Button(
//                onClick = onToggleRunning
                onClick={onRestart}
            ) {
//                Icon(
//                    imageVector = if(state == TimerState.RUNNING) {
//                        Icons.Default.Pause
//                    }else {
//                        Icons.Default.PlayArrow
//                    },
//                    contentDescription = null
//                )
                Text(
//                    text = if(state == TimerState.RUNNING) {
//                        "Pause"
//                    }else {
//                        "Start"
//                    }
                    text="Restart"
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                // onClick = onReset,
                onClick = onStop,
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
//                Icon(
//                    imageVector = Icons.Default.Stop,
//                    contentDescription = null
//                )
                Text(text = "Stop")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){

//            Text(text = "gx: ${String.format("%.2f",sides)}", fontSize = 20.sp)
////            Spacer(modifier = Modifier.width(2.dp))
//            Spacer(modifier = Modifier.height(1.dp))
//            Text(text = "gy: ${String.format("%.2f",upDown)}", fontSize = 20.sp)
////            Spacer(modifier = Modifier.width(2.dp))
//            Spacer(modifier = Modifier.height(1.dp))
            Text(text = "gz: ${String.format("%.2f",gz)}", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = "maxGz: ${String.format("%.2f",gzMax)}", fontSize = 20.sp)
        }
    }
}
