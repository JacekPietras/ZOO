package com.jacekpietras.zoo.data.gps

import android.content.Context
import timber.log.Timber

//data class LocationSimpleTracker(val context: Context) {
//    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
//    private var locationCallback: LocationCallback? = null
//
//    fun detectGPS(onGPSChanged: (Boolean) -> Unit) {
//        locationCallback = object : LocationCallback() {
//
//            override fun onLocationAvailability(var1: LocationAvailability?) {
//                Timber.i("GPS enabled: ${var1?.isLocationAvailable}")
//                onGPSChanged(var1?.isLocationAvailable ?: false)
//            }
//
//            override fun onLocationResult(result: LocationResult?) {
//                Timber.i("New location: ${result?.lastLocation}")
//            }
//        }
//        fusedLocationProviderClient.requestLocationUpdates(buildLocationRequest(), locationCallback, null)
//    }
//
//    private fun buildLocationRequest(): LocationRequest = LocationRequest.create().apply {
//        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        interval = 5000 //5 seconds
//        fastestInterval = 5000 //5 seconds
//        maxWaitTime = 1000 //1 seconds
//    }
//
//    fun stop() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
//    }
//}