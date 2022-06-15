package com.jacekpietras.zoo.tracking

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.*
import android.os.Looper
import androidx.core.content.ContextCompat.checkSelfPermission
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import timber.log.Timber


@SuppressLint("MissingPermission")
class GpsLocationListenerCompat(
    private val onLocationChanged: (time: Long, lat: Double, lon: Double) -> Unit,
    private val onGpsStatusChanged: (enabled: Boolean) -> Unit = {},
) {

    private var haveGooglePlay = false

    fun addLocationListener(context: Context) {
        haveGooglePlay = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        if (haveGooglePlay) addListenerWithGMS(context.applicationContext)
        else addListenerWithoutGMS(context.applicationContext)
    }

    fun removeLocationListener() {
        if (haveGooglePlay) removeListenerWithGMS()
        else removeListenerWithoutGMS()
    }

    fun noPermissions(context: Context): Boolean =
        checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
                checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED

    //region Without Google Play
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListenerImpl? = null

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
        val locationListener = locationListener ?: return false
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
        if (location != null) locationListener?.onLocationChanged(location)
    }

    private fun addListenerWithoutGMS(context: Context) {
        locationListener?.let { listener ->
            locationManager?.removeUpdates(listener)
        }

        locationManager = context.getSystemService(Service.LOCATION_SERVICE) as? LocationManager
        locationListener = LocationListenerImpl(onLocationChanged, onGpsStatusChanged)
        locationManager?.let { manager ->
            locationListener?.let { listener ->
                manager.removeUpdates(listener)
            }
            requestLocationUpdates(context, manager)
        }
    }

    private fun removeListenerWithoutGMS() {
        locationListener?.let { listener ->
            locationManager?.removeUpdates(listener)
        }
        locationListener = null
        locationManager = null
    }

    private class LocationListenerImpl(
        private val onLocationChanged: (time: Long, lat: Double, lon: Double) -> Unit,
        private val onGpsStatusChanged: (enabled: Boolean) -> Unit = {},
    ) : LocationListener {

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
    //endregion

    //region With Google Play
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private fun buildLocationRequest() =
        LocationRequest
            .create()
            .apply {
                priority = Priority.PRIORITY_HIGH_ACCURACY
                interval = 100
                fastestInterval = 100
                smallestDisplacement = 1f
            }

    private fun addListenerWithGMS(context: Context) {
        locationCallback?.let { locationCallback ->
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient = client
        locationCallback = LocationCallbackImpl(
            onLocationChanged,
            onGpsStatusChanged,
        ).also { locationCallback ->
            client.requestLocationUpdates(
                buildLocationRequest(),
                locationCallback,
                Looper.getMainLooper(),
            )
        }
    }

    private fun removeListenerWithGMS() {
        locationCallback?.let { locationCallback ->
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
        locationCallback = null
        fusedLocationClient = null
    }

    private class LocationCallbackImpl(
        private val onLocationChanged: (time: Long, lat: Double, lon: Double) -> Unit,
        private val onGpsStatusChanged: (enabled: Boolean) -> Unit = {},
    ) : LocationCallback() {

        override fun onLocationAvailability(available: LocationAvailability) {
            onGpsStatusChanged(available.isLocationAvailable)
        }

        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                onLocationChanged(
                    location.time,
                    location.latitude,
                    location.longitude,
                )
            }
        }
    }
    //endregion
}