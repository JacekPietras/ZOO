package com.jacekpietras.zoo.tracking

import android.Manifest.permission.*
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.provider.Settings
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.annotation.StringRes
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
            if (isGranted.filter { it.value }.isNotEmpty()) checkPermissionsAgain()
            else callbacks.onFailed()
        }
    private val resolutionResult =
        fragment.registerForActivityResult(StartIntentSenderForResult()) { isGranted ->
            if (isGranted.resultCode == RESULT_OK) checkPermissionsAgain()
            else callbacks.onFailed()
        }
    private lateinit var callbacks: Callback
    private var rationaleTitle: String = ""
    private var rationaleContent: String = ""
    private var deniedTitle: String = ""
    private var deniedContent: String = ""

    fun checkPermissions(
        @StringRes rationaleTitle: Int = 0,
        @StringRes rationaleContent: Int = 0,
        @StringRes deniedTitle: Int = 0,
        @StringRes deniedContent: Int = 0,
        onPermission: () -> Unit,
        onFailed: () -> Unit = {},
    ) {
        this.activity = fragment.requireActivity().unwrap()
        if (rationaleTitle != 0) {
            this.rationaleTitle = activity.getString(rationaleTitle)
            this.rationaleContent = activity.getString(rationaleContent)
        }
        if (deniedTitle != 0) {
            this.deniedTitle = activity.getString(deniedTitle)
            this.deniedContent = activity.getString(deniedContent)
        }

        this.callbacks = Callback(
            onFailed = onFailed,
            onPermission = onPermission,
        )
        checkPermissionsAgain()
    }

    private fun checkPermissionsAgain() {
        when {
            havePermissions() -> {
                resetFirstTimeAsking()

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
            shouldDescribe(ACCESS_FINE_LOCATION) ||
                    shouldDescribe(ACCESS_COARSE_LOCATION) ||
                    (SDK_INT >= VERSION_CODES.Q && shouldDescribe(ACCESS_BACKGROUND_LOCATION)) -> {
                // do nothing, dialog is shown
            }
            else -> askForPermissions()
        }
    }

    private fun askForPermissions() {
        askedForPermission()
        permissionResult.launch(
            listOfNotNull(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                if (SDK_INT >= VERSION_CODES.Q) ACCESS_BACKGROUND_LOCATION else null
            ).toTypedArray()
        )
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

    private fun shouldDescribe(permission: String): Boolean {
        val result = shouldShowRequestPermissionRationale(activity, permission)
        if (result) {
            return if (rationaleTitle.isNotEmpty()) {
                showRationale()
                true
            } else {
                false
            }
        }

        return if (isFirstTimeAskingPermission(permission)) {
            firstTimeAskingPermission(permission, false)
            false
        } else {
            if (deniedTitle.isNotEmpty()) {
                showDenied()
                true
            } else {
                false
            }
        }
    }

    private fun showRationale() {
        AlertDialog.Builder(activity)
            .setTitle(rationaleTitle)
            .setMessage(rationaleContent)
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                askForPermissions()
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, _ ->
                callbacks.onFailed()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDenied() {
        AlertDialog.Builder(activity)
            .setTitle(deniedTitle)
            .setMessage(deniedContent)
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()

                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(intent)
                observeReturn()
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, _ ->
                callbacks.onFailed()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun havePermissions(): Boolean =
        granted(ACCESS_FINE_LOCATION) &&
                granted(ACCESS_COARSE_LOCATION) &&
                (SDK_INT < VERSION_CODES.Q || granted(ACCESS_BACKGROUND_LOCATION))

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
                    checkPermissionsAgain()
                    fragment.lifecycle.removeObserver(this)
                }
            }
        })
    }

    private fun resetFirstTimeAsking() {
        firstTimeAskingPermission(ACCESS_FINE_LOCATION, false)
        firstTimeAskingPermission(ACCESS_COARSE_LOCATION, false)
        if (SDK_INT >= VERSION_CODES.Q) {
            firstTimeAskingPermission(ACCESS_BACKGROUND_LOCATION, false)
        }
    }

    private fun askedForPermission() {
        firstTimeAskingPermission(ACCESS_FINE_LOCATION, true)
        firstTimeAskingPermission(ACCESS_COARSE_LOCATION, true)
        if (SDK_INT >= VERSION_CODES.Q) {
            firstTimeAskingPermission(ACCESS_BACKGROUND_LOCATION, false)
        }
    }

    private fun firstTimeAskingPermission(permission: String, firstTime: Boolean) {
        val sharedPreference = activity.getSharedPreferences("GPS_PERMISSIONS", MODE_PRIVATE)
        sharedPreference.edit().putBoolean(permission, firstTime).apply()
    }

    private fun isFirstTimeAskingPermission(permission: String) =
        activity.getSharedPreferences("GPS_PERMISSIONS", MODE_PRIVATE).getBoolean(permission, true);

    private companion object {
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 6670
    }

    class Callback(
        val onPermission: () -> Unit,
        val onFailed: () -> Unit,
    )
}