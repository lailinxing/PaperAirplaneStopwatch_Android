package com.example.wearstopwatch.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StopWatchViewModel: ViewModel() {

    val _gz = MutableStateFlow(0L)
    val gz = _gz.asStateFlow()

    val _gzMax = MutableStateFlow(0L)
    val gzMax = _gzMax.asStateFlow()

    private  val _elapsedTime = MutableStateFlow(0L)
    private  val _timerState = MutableStateFlow(TimerState.STOP);
    val timerState = _timerState.asStateFlow()

    private  val formmater = DateTimeFormatter.ofPattern("HH:mm:ss:SSS")
    val stopWatchText = _elapsedTime
        .map { millis ->
            LocalTime.ofNanoOfDay((millis * 1_000_000)).format(formmater)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "00:00:00:000"
        )

    init {
        _timerState
            .flatMapLatest { timerState->
                getTimerFlow(
                    isRunning = (timerState == TimerState.RUNNING)
                )
            }
            .onEach { timeDiff ->
                _elapsedTime.update { it + timeDiff }
            }
            .launchIn((viewModelScope))
    }

    fun toggleIsRunning(){
        when(timerState.value) {
            TimerState.RUNNING -> _timerState.update { TimerState.PAUSED }
            TimerState.PAUSED,
            TimerState.STOP,
            TimerState.RESET -> _timerState.update { TimerState.RUNNING }
        }
    }

    fun stopCount(){
        _timerState.update { TimerState.STOP }

    }

    fun restart(){
        _timerState.update { TimerState.RUNNING }
        _elapsedTime.update { 0L }
    }

    fun resetTimer(){
        _timerState.update { TimerState.RESET }
        _elapsedTime.update { 0L }
    }

    private fun getTimerFlow(isRunning: Boolean):Flow<Long> {
        return flow {
            var startMillis = System.currentTimeMillis()

            while(isRunning){
                val currentMillis = System.currentTimeMillis();
                val timeDiff = if(currentMillis > startMillis){
                    currentMillis - startMillis
                }else 0L

                emit(timeDiff)
                startMillis = System.currentTimeMillis();
                delay(10L)
            }
        }
    }
}