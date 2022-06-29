package com.tvs.vitalsign.ui.vital_sensor

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.google.gson.Gson
//import com.iitp.signagekiosk.util.logD
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*

private typealias DoNothing = Unit

private const val address = "E4:5F:01:26:64:3A"
//private const val address = "DC:A6:32:B6:B9:C4"

private const val uuid = "00001101-0000-1000-8000-00805f9b34fb"

@SuppressLint("MissingPermission")
private fun BluetoothDevice.createBluetoothSocket(uuid: String): BluetoothSocket {
    return createRfcommSocketToServiceRecord(UUID.fromString(uuid))
}

class Sensor {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val device = bluetoothAdapter.getRemoteDevice(address)
    private var bluetoothSocket = device.createBluetoothSocket(uuid)

    private val scope = CoroutineScope(context = Dispatchers.IO)

    val isConnected = callbackFlow {
        while (isActive) {
            send(bluetoothSocket.isConnected)
            delay(2000L)
        }
    }.catch {
        it.printStackTrace()
    }.distinctUntilChanged().shareIn(
        scope = CoroutineScope(context = Dispatchers.IO),
        started = SharingStarted.WhileSubscribed()
    )

    private var fallbackSensorData = SensorData()

    @SuppressLint("MissingPermission")
    private val rawSensorData = channelFlow {
        bluetoothSocket.runCatching {
            connect()
        }.getOrThrow()

        val mmInStream = bluetoothSocket.inputStream
        val mmBuffer = ByteArray(1024)
        var numBytes = -1

        while (isActive) {
            mmInStream.runCatching {
                numBytes = read(mmBuffer)
            }.getOrThrow()
            send(numBytes to mmBuffer)
        }
        awaitClose { bluetoothSocket.close() }
    }.map { (numBytes, buffer) ->
        val jsonData = buffer.decodeToString(0, numBytes)
        jsonData.runCatching {
            Gson().fromJson(this, SensorData::class.java).also {
                fallbackSensorData = it
            }
        }.getOrElse { e ->
            e.printStackTrace()
            //logD("json parse error ||| $jsonData")
            fallbackSensorData
        }
    }.retry { cause ->
        bluetoothSocket.runCatching { close() }
        bluetoothSocket = device.createBluetoothSocket(uuid)
        cause.printStackTrace()
        delay(timeMillis = 1000)
        true
    }.shareIn(
        scope = scope,
        started = SharingStarted.Lazily
    )

    val receiveSensorData = rawSensorData.map { sensorData ->
        sensorData.copy(
            yValue = sensorData.yValue.mapIndexed { i, y -> y + correction[i] }
        )
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed()
    )

    private val detectThreshold = 3500L
    private val detectPredicate: suspend (SensorData) -> Boolean = {
        it.yValue.maxOrNull() ?: 0.0 >= 0.0
    }

    private val correction = DoubleArray(28) { 0.0 }

    fun calibration() {
        CoroutineScope(context = Dispatchers.IO).launch {
            val bufferSize = 10
            val buffer = mutableListOf<List<Double>>()
            rawSensorData.map { it.yValue }.collect {
                buffer.add(it)
                if (buffer.size == bufferSize) {
                    val yValue = buffer.reduce { acc, list ->
                        acc.zip(list) { y1, y2 -> y1 + y2 }
                    }.map { it.div(bufferSize) }
                    yValue.forEachIndexed { i, y ->
                        correction[i] = -detectThreshold - y
                    }
                    cancel()
                }
            }
        }
    }

    init {
        calibration()
    }

    private val isDetected = channelFlow {
        val bufferSize = 3
        val buffer = mutableListOf<Boolean>()
        receiveSensorData
            .map(detectPredicate)
            .collect {
                buffer.add(it)
                if (buffer.size == bufferSize) {
                    val detectedCnt = buffer.fold(0) { count, detected ->
                        count + if (detected) 1 else 0
                    }
                    send(detectedCnt > (bufferSize / 2))
                    buffer.clear()
                }
            }
    }

    private data class RangeDataSet(
        val xRange: List<Double>,
        val yValue: List<Double>,
    ) {
        val peakDistance =
            Distance.fromDouble(xRange.zip(yValue).maxByOrNull { (_, y) -> y }?.first ?: 0.0)
    }

    private enum class Distance {
        D50, D60, D70, D80, D90, D100, D110, D120, NONE;

        companion object {
            fun fromDouble(value: Double): Distance {
                return when (value) {
                    in 0.45..0.55 -> D50
                    in 0.55..0.65 -> D60
                    in 0.65..0.75 -> D70
                    in 0.75..0.85 -> D80
                    in 0.85..0.95 -> D90
                    in 0.95..1.05 -> D100
                    in 1.05..1.15 -> D110
                    in 1.15..1.25 -> D120
                    else -> NONE
                }
            }
        }
    }

    private val _motion = MutableSharedFlow<Boolean>()
    val motion = _motion.asSharedFlow()

    suspend fun startSensorMeasurement(): SensorData = coroutineScope {
        val hasMotion: suspend (SensorData) -> Boolean = { it.motion == 1 }
        val job = launch { receiveSensorData.collect { _motion.emit(hasMotion(it)) } }
        //delay(20000L)
        //job.cancel()
        return@coroutineScope receiveSensorData.filterNot(hasMotion).first().also {
            _motion.emit(false)
        }
    }

    private var state = Detection.IDLE

    fun resetState() {
        state = Detection.IDLE
    }

    val detection = channelFlow {
        val noUserInterval = 2000L
        val idleInterval = 4000L
        var idleCounterJob: Job? = null

        suspend fun send(detection: Detection) {
            state = detection
            this@channelFlow.send(detection)
        }

        isDetected.collect { isDetected ->
            if (isDetected) {
                idleCounterJob?.cancel()
                when (state) {
                    Detection.NEW_USER,
                    Detection.NO_USER,
                    -> send(Detection.ACTIVE)
                    Detection.IDLE -> send(Detection.NEW_USER)
                    else -> DoNothing
                }
            } else {
                when (state) {
                    Detection.NEW_USER,
                    Detection.ACTIVE,
                    -> if (idleCounterJob?.isActive != true) {
                        idleCounterJob = launch {
                            delay(noUserInterval)
                            send(Detection.NO_USER)
                            delay(idleInterval)
                            calibration()
                            send(Detection.IDLE)
                        }
                    }
                    else -> DoNothing
                }
            }
        }
        awaitClose { idleCounterJob?.cancel() }
    }.distinctUntilChanged().shareIn(
        scope = scope,
        started = SharingStarted.Lazily
    )

    private val distance = receiveSensorData
        .filter(detectPredicate)
        .map {
            RangeDataSet(xRange = it.xRange, yValue = it.yValue).peakDistance
        }


    val positionState = channelFlow {
        val bufferSize = 3
        val buffer = mutableMapOf<PositionState, Int>()
        distance.combine(detection.onStart { emit(Detection.ACTIVE) }) { distance, detection ->
            when {
                detection == Detection.NO_USER -> PositionState.GONE
                distance in Distance.D50..Distance.D60 -> PositionState.CLOSE
                distance in Distance.D70..Distance.D110 -> PositionState.FIT
                distance in Distance.D110..Distance.D120 -> PositionState.FAR
                else -> PositionState.GONE
            }
        }.collect {
            buffer[it] = buffer.getOrDefault(it, 0) + 1
            if (buffer.values.sum() == bufferSize) {
                val positionState = buffer.maxByOrNull { it.value }?.key ?: PositionState.GONE
                send(positionState)
                buffer.clear()
            }
        }
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed()
    )

    private val measurablePositionState = PositionState.FIT
    val isMeasurable = positionState.map { it == measurablePositionState }
}

enum class Detection {
    NEW_USER, ACTIVE, NO_USER, IDLE
}

enum class PositionState {
    CLOSE, FIT, FAR, GONE
}