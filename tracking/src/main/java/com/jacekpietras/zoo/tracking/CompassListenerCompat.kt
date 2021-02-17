package com.jacekpietras.zoo.tracking

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_MAGNETIC_FIELD
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

class CompassListenerCompat(
    private val onAngleChanged: (Float) -> Unit,
) {

    private val coroutine = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val r = FloatArray(9)
    private val i = FloatArray(9)
    private val orientation = FloatArray(3)
    private var lastAzimuth = 0.0

    private var sensorManager: SensorManager? = null
    private var listener: SensorEventListener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                coroutine.launch {
                    if (mutex.isLocked) return@launch
                    mutex.withLock {
                        calculateAzimuth(event)
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    fun addCompassListener(context: Context) {
        sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(TYPE_ACCELEROMETER)
        val magnetic = sensorManager?.getDefaultSensor(TYPE_MAGNETIC_FIELD)
        sensorManager?.registerListener(listener, accelerometer, SENSOR_DELAY_NORMAL)
        sensorManager?.registerListener(listener, magnetic, SENSOR_DELAY_NORMAL)
    }

    fun removeCompassListener() {
        sensorManager?.unregisterListener(listener)
        sensorManager = null
    }

    private fun calculateAzimuth(event: SensorEvent) {
        when (event.sensor.type) {
            TYPE_ACCELEROMETER -> {
                mGravity[0] = alpha * mGravity[0] + alphaRev * event.values[0]
                mGravity[1] = alpha * mGravity[1] + alphaRev * event.values[1]
                mGravity[2] = alpha * mGravity[2] + alphaRev * event.values[2]
            }
            TYPE_MAGNETIC_FIELD -> {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + alphaRev * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + alphaRev * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + alphaRev * event.values[2]
            }
            else -> return
        }
        val success = SensorManager.getRotationMatrix(r, i, mGravity, mGeomagnetic)
        if (success) {
            SensorManager.getOrientation(r, orientation)
            val azimuth = (Math.toDegrees(orientation[0].toDouble()) + 360) % 360
            if (abs(azimuth - lastAzimuth) > diff) {
                lastAzimuth = azimuth
                onAngleChanged(azimuth.toFloat())
            }
        }
    }

    private companion object {
        const val diff = 5f
        const val alpha = 0.97f
        const val alphaRev = 1 - alpha
    }
}