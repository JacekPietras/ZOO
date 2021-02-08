@file:Suppress("DEPRECATION")
@file:SuppressLint("MissingPermission")

package com.jacekpietras.zoo.tracking

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Build
import timber.log.Timber

class GpsStatusListenerCompat(onStatusChanged: (enabled: Boolean) -> Unit) {

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

    companion object {

        fun LocationManager.addStatusListener(listener: GpsStatusListenerCompat) {
            try {
                if (listener.statusListener is GpsStatus.Listener) {
                    addGpsStatusListener(listener.statusListener)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                    listener.statusListener is GnssStatus.Callback
                ) {
                    registerGnssStatusCallback(listener.statusListener)
                }
            } catch (e: SecurityException) {
                Timber.w(e, "Location permissions not granted")
            }
        }

        fun LocationManager.removeStatusListener(listener: GpsStatusListenerCompat) {
            if (listener.statusListener is GpsStatus.Listener) {
                removeGpsStatusListener(listener.statusListener)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                listener.statusListener is GnssStatus.Callback
            ) {
                unregisterGnssStatusCallback(listener.statusListener)
            }
        }
    }
}
