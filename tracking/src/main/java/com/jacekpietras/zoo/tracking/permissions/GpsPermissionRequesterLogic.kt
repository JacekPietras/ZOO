package com.jacekpietras.zoo.tracking.permissions

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.lifecycle.LifecycleOwner
import com.jacekpietras.zoo.tracking.R
import com.jacekpietras.zoo.tracking.permissions.checker.AndroidPermissionChecker
import com.jacekpietras.zoo.tracking.permissions.checker.PermissionChecker
import com.jacekpietras.zoo.tracking.utils.getApplicationSettingsIntent
import com.jacekpietras.zoo.tracking.utils.isGpsEnabled
import com.jacekpietras.zoo.tracking.utils.observeReturn
import timber.log.Timber

class GpsPermissionRequesterLogic(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
) {

    private lateinit var callbacks: Callback
    private val permissionChecker: PermissionChecker = AndroidPermissionChecker(
        context = activity,
    )

    fun checkPermissions(
        permissionRequest: (List<String>) -> Unit,
        enableGpsRequest: (PendingIntent) -> Unit,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
    ) {
        this.callbacks = Callback(
            permissionRequest = permissionRequest,
            resolutionRequest = enableGpsRequest,
            onFailed = onDenied,
            onPermission = onGranted,
        )
        checkPermissionsAgain()
    }

    private fun notifyFailed() {
        callbacks.onFailed()
    }

    private fun checkPermissionsAgain() {
        when {
            allPermissionsAreGranted() -> {
                allPermissions.forEach(::removeAsDeniedForever)
                enableGPS()
            }
            notGrantedNeededPermissions.firstOrNull(::shouldShowRationale) != null -> {
                showRationale()
            }
            notGrantedNeededPermissions.firstOrNull(::shouldShowDeniedForever) != null -> {
                showDenied()
            }
            else -> {
                askForPermissions()
            }
        }
    }

    private fun enableGPS() {
        if (activity.isGpsEnabled()) {
            callbacks.onPermission()
        } else {
            EnableGpsUseCase().run(
                activity = activity,
                lifecycleOwner = lifecycleOwner,
                onRequestSth = { intent -> callbacks.resolutionRequest(intent) },
                onFreshRequestRequired = { checkPermissionsAgain() },
                onGpsEnabled = { callbacks.onPermission() },
                onDenied = { callbacks.onFailed() },
            )
        }
    }

    private fun shouldShowRationale(permission: String): Boolean =
        shouldShowRequestPermissionRationale(activity, permission)

    private fun shouldShowDeniedForever(permission: String): Boolean =
        isDeniedForever(permission)

    private fun askForPermissions() {
        callbacks.permissionRequest(notGrantedAllPermissions)
    }

    private fun showRationale() {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.gps_permission_rationale_title))
            .setMessage(activity.getString(R.string.gps_permission_rationale_content))
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
            .setTitle(activity.getString(R.string.gps_permission_denied_title))
            .setMessage(activity.getString(R.string.gps_permission_denied_content))
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                try {
                    activity.startActivity(activity.getApplicationSettingsIntent())
                    lifecycleOwner.observeReturn { checkPermissionsAgain() }
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e, "Asking permissions - Cannot open settings")
                    showDeniedWithoutCondition()
                }
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, _ ->
                callbacks.onFailed()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeniedWithoutCondition() {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.gps_permission_denied_title))
            .setMessage(activity.getString(R.string.gps_permission_cannot_open_settings_content))
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                callbacks.onFailed()
                dialog.dismiss()
            }
            .create()
            .show()
    }


    private fun allPermissionsAreGranted(): Boolean =
        permissionChecker.hasPermissions(neededPermissions)

    private val notGrantedNeededPermissions: List<String>
        get() = neededPermissions.filterNot(::isGranted)

    private val notGrantedAllPermissions: List<String>
        get() = allPermissions.filterNot(::isGranted)

    private fun isGranted(permission: String): Boolean =
        permissionChecker.hasPermissions(listOf(permission))

    private fun setAsDeniedForever(permission: String) {
        activity.getSharedPreferences(GPS_DENIED_FOREVER_KEY, MODE_PRIVATE)
            .edit()
            .putBoolean(permission, true)
            .apply()
    }

    private fun removeAsDeniedForever(permission: String) {
        activity.getSharedPreferences(GPS_DENIED_FOREVER_KEY, MODE_PRIVATE)
            .edit()
            .remove(permission)
            .apply()
    }

    private fun isDeniedForever(permission: String) =
        activity.getSharedPreferences(GPS_DENIED_FOREVER_KEY, MODE_PRIVATE)
            .getBoolean(permission, false)

    fun onPermissionsRequested(isGranted: Map<String, Boolean>) {
        isGranted
            .filter { !it.value && !shouldShowRequestPermissionRationale(activity, it.key) }
            .keys
            .forEach(::setAsDeniedForever)

        isGranted
            .filter { it.value }
            .keys
            .forEach(::removeAsDeniedForever)

        if (isGranted.filter { it.value }.isNotEmpty()) {
            checkPermissionsAgain()
        } else {
            notifyFailed()
        }
    }

    fun onEnablingGpsRequested(isGranted: ActivityResult){
        if (isGranted.resultCode == Activity.RESULT_OK) {
            checkPermissionsAgain()
        } else {
            notifyFailed()
        }
    }

    private class Callback(
        val permissionRequest: (List<String>) -> Unit,
        val resolutionRequest: (PendingIntent) -> Unit,
        val onPermission: () -> Unit,
        val onFailed: () -> Unit,
    )

    private companion object {

        val allPermissions = listOfNotNull(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            if (SDK_INT >= VERSION_CODES.Q) ACCESS_BACKGROUND_LOCATION else null
        )
        val neededPermissions = listOfNotNull(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
        )
        const val GPS_DENIED_FOREVER_KEY = "GPS_DENIED_FOREVER_KEY"
    }
}