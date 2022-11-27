package com.jacekpietras.zoo.tracking.permissions

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.jacekpietras.zoo.tracking.R
import com.jacekpietras.zoo.tracking.utils.getApplicationSettingsIntent
import com.jacekpietras.zoo.tracking.utils.isGpsEnabled
import com.jacekpietras.zoo.tracking.utils.observeReturn
import timber.log.Timber

class GpsPermissionRequesterLogic(
    private val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
) {

    private lateinit var callbacks: Callback

    fun checkPermissions(
        permissionRequest: (List<String>) -> Unit,
        resolutionRequest: (PendingIntent) -> Unit,
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
    ) {
        this.callbacks = Callback(
            permissionRequest = permissionRequest,
            resolutionRequest = resolutionRequest,
            onFailed = onDenied,
            onPermission = onGranted,
        )
        checkPermissionsAgain()
    }

    fun notifyFailed() {
        callbacks.onFailed()
    }

    fun checkPermissionsAgain() {
        when {
            havePermissions() -> {
                resetFirstTimeAsking()

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
            allPermissions.any { shouldDescribe(it) } -> {
                // do nothing, dialog is shown
            }
            else -> {
                askedForPermission()
                askForPermissions()
            }
        }
    }

    private fun shouldDescribe(permission: String): Boolean =
        when {
            shouldShowRequestPermissionRationale(activity, permission) -> {
                showRationale()
                true
            }
            isFirstTimeAskingPermission(permission) -> {
                firstTimeAskingPermission(permission, false)
                false
            }
            else -> {
                showDenied()
                true
            }
        }

    private fun askForPermissions() {
        callbacks.permissionRequest(allPermissions)
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

    private fun havePermissions(): Boolean =
        neededPermissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun resetFirstTimeAsking() {
        allPermissions.forEach {
            firstTimeAskingPermission(it, false)
        }
    }

    private fun askedForPermission() {
        allPermissions.forEach {
            firstTimeAskingPermission(it, true)
        }
    }

    private fun firstTimeAskingPermission(permission: String, firstTime: Boolean) {
        val sharedPreference = activity.getSharedPreferences(GPS_PERMISSIONS, MODE_PRIVATE)
        sharedPreference.edit().putBoolean(permission, firstTime).apply()
    }

    private fun isFirstTimeAskingPermission(permission: String) =
        activity.getSharedPreferences(GPS_PERMISSIONS, MODE_PRIVATE).getBoolean(permission, true)

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
        const val GPS_PERMISSIONS = "GPS_PERMISSIONS"
    }
}