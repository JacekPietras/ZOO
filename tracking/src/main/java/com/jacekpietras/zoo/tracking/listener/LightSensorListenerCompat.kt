package com.jacekpietras.zoo.tracking.listener

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL

class LightSensorListenerCompat(
    private val onLightChanged: (value:Float) -> Unit,
) {

    private var sensorManager: SensorManager? = null
    private var listener: SensorEventListener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                onLightChanged.invoke(event.values[0])
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    fun addLightSensorListener(context: Context) {
        sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager?.registerListener(listener, lightSensor, SENSOR_DELAY_NORMAL)
    }

    fun removeLightSensorListener() {
        sensorManager?.unregisterListener(listener)
        sensorManager = null
    }
}