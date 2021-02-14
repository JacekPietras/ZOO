@file:Suppress("DEPRECATION")
@file:SuppressLint("MissingPermission")

package com.jacekpietras.zoo.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Build
import timber.log.Timber

class GpsStatusListenerCompat(onStatusChanged: (enabled: Boolean) -> Unit) {

    private var locationManager: LocationManager? = null
    private val statusListener: Any = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                Timber.i("Gps Status $status")
                onStatusChanged(status.satelliteCount > 3)
            }
        }
    } else {
        GpsStatus.Listener {
            when (it) {
                GpsStatus.GPS_EVENT_STARTED -> onStatusChanged(true)
                GpsStatus.GPS_EVENT_STOPPED -> onStatusChanged(false)
            }
        }
    }

    fun addStatusListener(context: Context) {
        locationManager = context.applicationContext
            .getSystemService(Service.LOCATION_SERVICE) as? LocationManager
        try {
            if (statusListener is GpsStatus.Listener) {
                locationManager?.addGpsStatusListener(statusListener)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                statusListener is GnssStatus.Callback
            ) {
                locationManager?.registerGnssStatusCallback(statusListener)
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Location permissions not granted")
        }
    }

    fun removeStatusListener() {
        if (statusListener is GpsStatus.Listener) {
            locationManager?.removeGpsStatusListener(statusListener)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            statusListener is GnssStatus.Callback
        ) {
            locationManager?.unregisterGnssStatusCallback(statusListener)
        }
        locationManager = null
    }
}
