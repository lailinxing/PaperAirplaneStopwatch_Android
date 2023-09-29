package com.example.wearstopwatch.presentation
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text



class MainActivity : ComponentActivity(), SensorEventListener
{
    private lateinit var sensorManager: SensorManager
    private val gx:MutableState<Float> = mutableStateOf(0f);
    private val gxMax:MutableState<Float> = mutableStateOf(0f);
    private val gxMin:MutableState<Float> = mutableStateOf(0f);
    private val gy:MutableState<Float> = mutableStateOf(0f);
    private val gyMax:MutableState<Float> = mutableStateOf(0f);
    private val gyMin:MutableState<Float> = mutableStateOf(0f);
    private val gz:MutableState<Float> = mutableStateOf(0f);
    private val gzMax:MutableState<Float> = mutableStateOf(0f);
    private val gzMin:MutableState<Float> = mutableStateOf(0f);

    private var isStarted:MutableState<Boolean> = mutableStateOf(false);
    private var duraTimeMillis:MutableState<Long> = mutableStateOf(0L);
    private var startTimeMillis = 0L;
    lateinit var timerHandler: Handler;
    var START_TIMER_GZ_THRESHOLD = 30f;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        setContent {
            StopWatch(
                isStarted = isStarted.value,
                duraTimeMillis = duraTimeMillis.value,
                onRestart = this::restart,
                onStop = this::stopCount,
                onClick = this::handleClick,

                gxMin=gxMin.value,
                gx = gx.value,
                gxMax = gxMax.value,

                gyMin=gyMin.value,
                gy = gy.value,
                gyMax = gyMax.value,

                gzMin=gzMin.value,
                gz = gz.value,
                gzMax = gzMax.value,
            );
        }

        setUpSensorStuff();
        timerHandler = Handler(Looper.getMainLooper());
    }


    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager;

        val mSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensor?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            );
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            var currentGx = event!!.values[0];
            var currentGy = event!!.values[1];
            var currentGz = event!!.values[2];

            gx.value = currentGx;
            gy.value = currentGy;
            gz.value  = currentGz;

            if(currentGx < gxMin.value){
                gxMin.value  = currentGx;

                if(currentGx < -20f){
                    restart();
                }
            }
            if(currentGx > gxMax.value){
                gxMax.value  = currentGx;
            }

            if(currentGy < gyMin.value){
                gyMin.value  = currentGy;
            }
            if(currentGy > gyMax.value){
                gyMax.value  = currentGy;
            }


            if(currentGz < gzMin.value){
                gzMin.value  = currentGz;
            }
            if(currentGz > gzMax.value){
                if(currentGz > START_TIMER_GZ_THRESHOLD){
//                    restart();
                }
                gzMax.value  = currentGz;
            }
//            println("x: ${event!!.values[0].toString()}");
//            println("y: ${event!!.values[1].toString()}");
//            println("z: ${event!!.values[2].toString()}");
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return;
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this);
        timerHandler.removeCallbacks(updateTextTask);
        super.onDestroy();
    }

    private val updateTextTask = object : Runnable {
        override fun run() {
            timerCallback();
            timerHandler.postDelayed(this, 10);
        }
    }

    fun timerCallback() {
        duraTimeMillis.value = System.currentTimeMillis() - startTimeMillis;
    }

    private fun restart(){
        timerHandler.removeCallbacks(updateTextTask);

        startTimeMillis = System.currentTimeMillis();
        isStarted.value = true;
        duraTimeMillis.value = 0L;

        timerHandler.post(updateTextTask);
    }

    private fun handleClick(){
        if(isStarted.value){
            stopCount();
        }else{
            duraTimeMillis.value  = 0L;
        }
    }

    private fun stopCount(){
        isStarted.value = false;

        gxMin.value = 0f;
        gxMax.value = 0f;

        gyMin.value = 0f;
        gyMax.value = 0f;

        gzMin.value = 0f;
        gzMax.value = 0f;

        timerHandler.removeCallbacks(updateTextTask);
    }
}

@Composable
private fun StopWatch(
    isStarted: Boolean,
    duraTimeMillis: Long,
    onRestart:() -> Unit,
    onStop: () -> Unit,
    onClick: () -> Unit,
    gxMin: Float,
    gx: Float,
    gxMax: Float,
    gyMin: Float,
    gy: Float,
    gyMax: Float,
    gzMin: Float,
    gz: Float,
    gzMax: Float,

){
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).clickable { onClick()},
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var timeSeconds = duraTimeMillis / 1000f;
        var prev = if( timeSeconds<10) "0" else "";

         Text(
            text = String.format(" ${prev}%.2fs", timeSeconds),
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraLight,
            textAlign = TextAlign.Center,
        )

//        Spacer(modifier = Modifier.height(8.dp))

//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ){
//            Button(
//                onClick = onRestart
//            ) {
////                Icon(
////                    imageVector = if(isStarted) {
////                        Icons.Default.Stop
////                    }else {
////                        Icons.Default.PlayArrow
////                    },
////                    contentDescription = null
////                )
//                Text(
//                    text="Restart"
//                )
//            }

//            Spacer(modifier = Modifier.width(4.dp))

//            Button(
//                onClick = onStop,
//                modifier = Modifier.size(width=80.dp, height = 80.dp)
//            ) {
//                Text(
//                    text = "Stop",
//                    color = Color.White
//                )
//            }
//        }

//        SensorInfo( gxMin = gxMin, gx = gx, gxMax = gxMax, gyMin = gyMin, gy = gy, gyMax = gyMax, gzMin = gzMin, gz = gz, gzMax = gzMax,);

    }
}

@Composable
private  fun SensorInfo(
    gxMin: Float,
    gx: Float,
    gxMax: Float,
    gyMin: Float,
    gy: Float,
    gyMax: Float,
    gzMin: Float,
    gz: Float,
    gzMax: Float,
){
    Spacer(modifier = Modifier.height(8.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val fontSize = 18.sp;
        Text(text = "gx: ${String.format("%.1f",gx)}(${String.format(" %.1f",gxMin)}:${String.format(" %.1f",gxMax)})", fontSize = fontSize)
        Text(text = "gy: ${String.format("%.1f",gy)} ((${String.format(" %.1f",gyMin)}:${String.format(" %.1f",gyMax)})", fontSize = fontSize)
        Text(
            text = " gz: ${String.format("%.1f",gz)} ((${String.format(" %.1f",gzMin)}:${String.format(" %.1f",gzMax)})",
            fontSize = fontSize
        )
    }
}
