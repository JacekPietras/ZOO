package com.jacekpietras.zoo.tracking

import android.Manifest.permission.*
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task

class GpsPermissionChecker(private val fragment: Fragment) {

    private lateinit var activity: AppCompatActivity
    private var promptForUpdatePlayServicesWasShown = false
    private var wasInSettingsToTurnOnLocation = false
    private var errorDialog: Dialog? = null
    private val permissionResult =
        fragment.registerForActivityResult(RequestMultiplePermissions()) { isGranted ->
            if (isGranted.filter { it.value }.isNotEmpty()) checkPermissions(callbacks)
            else callbacks.onFailed()
        }
    private val resolutionResult =
        fragment.registerForActivityResult(StartIntentSenderForResult()) { isGranted ->
            if (isGranted.resultCode == RESULT_OK) checkPermissions(callbacks)
            else callbacks.onFailed()
        }
    private lateinit var callbacks: Callback

    fun checkPermissions(
        onDescriptionNeeded: (String) -> Unit,
        onPermission: () -> Unit,
        onFailed: () -> Unit,
    ) {
        checkPermissions(
            Callback(
                onDescriptionNeeded = onDescriptionNeeded,
                onFailed = onFailed,
                onPermission = onPermission,
            )
        )
    }

    private fun checkPermissions(callbacks: Callback) {
        this.callbacks = callbacks
        this.activity = fragment.requireActivity().unwrap()
        when {
            havePermissions() -> {
                if (isGpsEnabled()) {
                    callbacks.onPermission()
                    TrackingService.start(activity)
                } else {
                    try {
                        askForGps(callbacks)
                    } catch (e: Exception) {
                        askForGpsInClassicWay(callbacks)
                    }
                }
            }
            shouldDescribe(ACCESS_FINE_LOCATION) -> {
                callbacks.onDescriptionNeeded(ACCESS_FINE_LOCATION)
            }
            shouldDescribe(ACCESS_COARSE_LOCATION) -> {
                callbacks.onDescriptionNeeded(ACCESS_COARSE_LOCATION)
            }
            SDK_INT >= VERSION_CODES.Q && shouldDescribe(ACCESS_BACKGROUND_LOCATION) -> {
                callbacks.onDescriptionNeeded(ACCESS_BACKGROUND_LOCATION)
            }
            else -> {
                permissionResult.launch(
                    listOfNotNull(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        if (SDK_INT >= VERSION_CODES.Q) ACCESS_BACKGROUND_LOCATION else null
                    ).toTypedArray()
                )
            }
        }
    }

    private fun askForGps(callbacks: Callback) {
        val mLocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(activity) {
            callbacks.onPermission()
            TrackingService.start(activity)
        }
        task.addOnFailureListener(activity) { e ->
            try {
                val resolvable = (e as? ResolvableApiException)
                if (resolvable != null) {
                    resolutionResult.launch(
                        IntentSenderRequest.Builder(resolvable.resolution).build()
                    )
                } else {
                    callbacks.onFailed()
                }
            } catch (ignored: SendIntentException) {
                callbacks.onFailed()
            }
        }
    }

    private fun askForGpsInClassicWay(callbacks: Callback) {
        val locationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (locationManager == null || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            wasInSettingsToTurnOnLocation = true
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(settingsIntent)
            observeReturn()
        } else {
            // playServicesAvailable - there are no play services installed,
            // maybe this is device without possibility of it
            if (checkPlayServices() && !promptForUpdatePlayServicesWasShown) {
                // showing dialog for updating google play services
                val googleAPI = GoogleApiAvailability.getInstance()
                val result = googleAPI.isGooglePlayServicesAvailable(activity)
                if (result != ConnectionResult.SUCCESS && googleAPI.isUserResolvableError(result)) {
                    showErrorDialog(googleAPI, result)
                    observeReturn()
                } else {
                    callbacks.onFailed()
                }
            } else {
                callbacks.onFailed()
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        return (activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
            .isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun shouldDescribe(permission: String): Boolean =
        shouldShowRequestPermissionRationale(activity, permission)

    private fun havePermissions(): Boolean =
        granted(ACCESS_FINE_LOCATION) && granted(ACCESS_COARSE_LOCATION)

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    private fun checkPlayServices(): Boolean =
        try {
            val googleServicesVersion =
                activity.packageManager.getPackageInfo("com.google.android.gms", 0).versionCompat

            // it is required version for checking location
            11200000 <= googleServicesVersion
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }

    @Suppress("DEPRECATION")
    private val PackageInfo.versionCompat: Long
        get() = if (SDK_INT >= VERSION_CODES.P) longVersionCode
        else versionCode.toLong()

    private fun showErrorDialog(googleAPI: GoogleApiAvailability, result: Int) {
        errorDialog = googleAPI
            .getErrorDialog(activity, result, PLAY_SERVICES_RESOLUTION_REQUEST)
            .apply {
                setOnDismissListener { errorDialog = null }
                show()
            }
        promptForUpdatePlayServicesWasShown = true
    }

    private fun Context.unwrap(): AppCompatActivity {
        var context = this
        while (context is ContextWrapper) {
            if (context is AppCompatActivity) {
                return context
            }
            context = context.baseContext
        }
        throw IllegalArgumentException("AppCompatActivity required")
    }

    private fun observeReturn() {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var stopped = false

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                stopped = true
            }

            override fun onResume(owner: LifecycleOwner) {
                if (stopped) {
                    checkPermissions(callbacks)
                    fragment.lifecycle.removeObserver(this)
                }
            }
        })
    }

    private companion object {
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 6670
    }

    class Callback(
        val onDescriptionNeeded: (String) -> Unit,
        val onPermission: () -> Unit,
        val onFailed: () -> Unit,
    )
}