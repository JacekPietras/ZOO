package com.jacekpietras.zoo.tracking.permissions

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.jacekpietras.zoo.tracking.utils.observeReturn

internal class EnableGpsUseCase {

    fun run(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        onRequestSth: (PendingIntent) -> Unit,
        onFreshRequestRequired: () -> Unit,
        onGpsEnabled: () -> Unit,
        onDenied: () -> Unit,
    ) {
        try {
            askForGps(activity, onGpsEnabled, onRequestSth, onDenied)
        } catch (e: Exception) {
            askForGpsInClassicWay(activity, lifecycleOwner, onFreshRequestRequired, onDenied)
        }
    }

    private fun askForGps(
        activity: Activity,
        onGpsEnabled: () -> Unit,
        onRequestSth: (PendingIntent) -> Unit,
        onDenied: () -> Unit,
    ) {
        val mLocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(activity) {
            onGpsEnabled()
        }
        task.addOnFailureListener(activity) { e ->
            try {
                val resolvable = (e as? ResolvableApiException)
                if (resolvable != null) {
                    onRequestSth(resolvable.resolution)
                } else {
                    onDenied()
                }
            } catch (ignored: IntentSender.SendIntentException) {
                onDenied()
            }
        }
    }

    private fun askForGpsInClassicWay(
        activity: Activity,
        lifecycleOwner: LifecycleOwner,
        onFreshRequestRequired: () -> Unit,
        onDenied: () -> Unit,
    ) {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(settingsIntent)
            lifecycleOwner.observeReturn(onFreshRequestRequired)

        } else {
            // playServicesAvailable - there are no play services installed,
            // maybe this is device without possibility of it
            if (checkPlayServices(activity) && !promptForUpdatePlayServicesWasShown) {
                // showing dialog for updating google play services
                val googleAPI = GoogleApiAvailability.getInstance()
                val result = googleAPI.isGooglePlayServicesAvailable(activity)
                if (result != ConnectionResult.SUCCESS && googleAPI.isUserResolvableError(result)) {
                    showErrorDialog(activity, googleAPI, result)
                    lifecycleOwner.observeReturn(onFreshRequestRequired)
                } else {
                    onDenied()
                }
            } else {
                onDenied()
            }
        }
    }

    private fun checkPlayServices(
        activity: Activity,
    ): Boolean =
        try {
            val googleServicesVersion = activity.packageManager.getPackageInfo("com.google.android.gms", 0).versionCompat

            // it is required version for checking location
            11200000 <= googleServicesVersion
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }

    @Suppress("DEPRECATION")
    private val PackageInfo.versionCompat: Long
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode
        else versionCode.toLong()

    private fun showErrorDialog(
        activity: Activity,
        googleAPI: GoogleApiAvailability,
        result: Int,
    ) {
        googleAPI
            .getErrorDialog(activity, result, PLAY_SERVICES_RESOLUTION_REQUEST)
            ?.apply {
                show()
                promptForUpdatePlayServicesWasShown = true
            }
    }

    private companion object {

        var promptForUpdatePlayServicesWasShown = false
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 6670
    }
}