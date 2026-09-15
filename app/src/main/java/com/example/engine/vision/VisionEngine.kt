package com.example.engine.vision

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

data class VisionInspectionResult(
    val detectedTexts: List<String>,
    val detectedObjects: List<String>,
    val uiTargetCoordinates: List<String>,
    val summary: String,
    val isStable: Boolean = true
)

class VisionEngine(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isFrameStable = MutableStateFlow(true)
    val isFrameStable: StateFlow<Boolean> = _isFrameStable.asStateFlow()

    private val _continuousModeActive = MutableStateFlow(false)
    val continuousModeActive: StateFlow<Boolean> = _continuousModeActive.asStateFlow()

    private val _latestInspection = MutableStateFlow<VisionInspectionResult?>(null)
    val latestInspection: StateFlow<VisionInspectionResult?> = _latestInspection.asStateFlow()

    private var lastAccelMag = 9.8f

    fun startContinuousInspection() {
        _continuousModeActive.value = true
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        performSimulatedCapture()
    }

    fun stopContinuousInspection() {
        _continuousModeActive.value = false
        sensorManager.unregisterListener(this)
    }

    fun inspectCurrentScene(prompt: String = "Read text and describe objects"): VisionInspectionResult {
        val result = VisionInspectionResult(
            detectedTexts = listOf("VoidCore Intelligence Architecture", "Meeting Schedule 9:30 AM", "Confidential Project Brief"),
            detectedObjects = listOf("Document Page", "Laptop Screen", "Coffee Mug"),
            uiTargetCoordinates = listOf("Button: 'Confirm Plan' at (340, 720)", "Input: 'Search' at (200, 180)"),
            summary = "Found document titled 'VoidCore Intelligence Architecture' and calendar event for 9:30 AM.",
            isStable = _isFrameStable.value
        )
        _latestInspection.value = result
        return result
    }

    private fun performSimulatedCapture() {
        if (_isFrameStable.value) {
            inspectCurrentScene()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val magnitude = sqrt(x * x + y * y + z * z)
        val delta = Math.abs(magnitude - lastAccelMag)
        lastAccelMag = magnitude

        // Shaky if delta > 1.8 m/s^2
        val stable = delta < 1.8f
        if (stable != _isFrameStable.value) {
            _isFrameStable.value = stable
            if (stable && _continuousModeActive.value) {
                performSimulatedCapture()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        @Volatile
        private var INSTANCE: VisionEngine? = null

        fun getInstance(context: Context): VisionEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VisionEngine(context).also { INSTANCE = it }
            }
        }
    }
}
