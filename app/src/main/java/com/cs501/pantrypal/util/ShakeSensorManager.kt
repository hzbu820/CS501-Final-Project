package com.cs501.pantrypal.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var accelerationCurrent = 0f
    private var accelerationLast = 0f
    private var accelerationDelta = 0f
    
    // Set shake detection threshold (adjust sensitivity as needed)
    private val shakeThreshold = 11.5f
    
    // To prevent multiple shake detections in a short time
    private var lastShakeTime = 0L
    private val minimumTimeBetweenShakes = 1000 // ms
    
    private var onShakeListener: (() -> Unit)? = null
    
    fun setOnShakeListener(listener: () -> Unit) {
        onShakeListener = listener
    }
    
    fun register() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    fun unregister() {
        sensorManager.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            accelerationLast = accelerationCurrent
            accelerationCurrent = sqrt(x * x + y * y + z * z)
            accelerationDelta = accelerationCurrent - accelerationLast
            
            // Check if the acceleration exceeds our threshold and enough time has passed since last shake
            if (accelerationDelta > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastShakeTime > minimumTimeBetweenShakes) {
                    lastShakeTime = currentTime
                    onShakeListener?.invoke()
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not needed for this implementation
    }
} 