package com.jacekpietras.zoo.tracking

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.*
import androidx.core.content.ContextCompat
import timber.log.Timber

@SuppressLint("MissingPermission")
class GpsLocationListenerCompat(
    onLocationChanged: (time: Long, lat: Double, lon: Double) -> Unit,
    onGpsStatusChanged: (enabled: Boolean) -> Unit = {},
) {

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            onLocationChanged(
                location.time,
                location.latitude,
                location.longitude
            )
        }

        override fun onProviderDisabled(provider: String) {
            onGpsStatusChanged(false)
        }

        override fun onProviderEnabled(provider: String) {
            onGpsStatusChanged(true)
        }
    }

    private fun requestLocationUpdates(
        context: Context,
        locationManager: LocationManager
    ): Boolean =
        locationManager.requestLocationUpdates(context, GPS_PROVIDER)
                || locationManager.requestLocationUpdates(context, NETWORK_PROVIDER)
                || locationManager.requestLocationUpdates(context, PASSIVE_PROVIDER)

    private fun LocationManager.requestLocationUpdates(
        context: Context,
        provider: String
    ): Boolean {
        when {
            noPermissions(context) -> Timber.e("Permissions not granted")
            allProviders.contains(provider) -> {
                try {
                    requestLocationUpdates(provider, 5000, 0f, locationListener)
                    updateWithLastKnownLocation(this, provider)
                    return true
                } catch (e: NullPointerException) {
                    Timber.w(e, "Location cannot be accessed")
                } catch (e: IllegalArgumentException) {
                    Timber.w(e, "Location cannot be accessed")
                }
            }
        }
        return false
    }

    private fun updateWithLastKnownLocation(locationManager: LocationManager, provider: String) {
        val location = locationManager.getLastKnownLocation(provider)
        if (location != null) locationListener.onLocationChanged(location)
    }

    fun noPermissions(context: Context): Boolean =
        !granted(context, ACCESS_FINE_LOCATION) && !granted(context, ACCESS_COARSE_LOCATION)

    private fun granted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED

    companion object {

        fun LocationManager.addLocationListener(
            context: Context,
            listener: GpsLocationListenerCompat,
        ): Boolean {
            removeUpdates(listener.locationListener)
            return listener.requestLocationUpdates(context, this)
        }

        fun LocationManager.removeLocationListener(listener: GpsLocationListenerCompat) {
            removeUpdates(listener.locationListener)
        }
    }
}