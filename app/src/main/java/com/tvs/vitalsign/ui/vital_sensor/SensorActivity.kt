package com.tvs.vitalsign.ui.vital_sensor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvs.vitalsign.ui.MainActivityViewModel
import com.tvs.vitalsign.ui.analysis.VitalAnalysisViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class QuestionnaireViewModel constructor(
    private val sensor: Sensor,
    ) : ViewModel() {
    init {

    }
    private fun startSensorMeasurement() {
        viewModelScope.launch {
                val sensorData = sensor.startSensorMeasurement()
                sensor.receiveSensorData.collect {
                    val respiration = "${sensorData.breath.toInt()}"
                    val heartRate = "${sensorData.heartRate.toInt()}"
                    val bodyTempValue = "%.1f".format(sensorData.bodyTemp)
                }
        }
    }
    }