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
    private var timeOfRequest: Long = 0L

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
        checkForParticularPermissions(
            permissions = neededPermissions,
            onGranted = {
                checkForParticularPermissions(
                    permissions = bonusPermissions,
                    onGranted = { enableGPS() },
                    onDenied = { enableGPS() },
                )
            },
            onDenied = { callbacks.onFailed() },
        )
    }

    private fun checkForParticularPermissions(
        permissions: List<String>,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        when {
            permissions.isEmpty() -> {
                onGranted()
            }
            permissionChecker.hasPermissions(permissions) -> {
                permissions.forEach(::removeAsDeniedForever)
                onGranted()
            }
            permissions.filterNot(::isGranted).firstOrNull(::shouldShowRationale) != null -> {
                showRationale(
                    onGranted = { askForPermissions(permissions) },
                    onDenied = onDenied,
                )
            }
            permissions.filterNot(::isGranted).firstOrNull(::shouldShowDeniedForever) != null -> {
                showDenied(
                    onGranted = { checkPermissionsAgain() },
                    onDenied = onDenied,
                )
            }
            else -> {
                askForPermissions(permissions)
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

    private fun askForPermissions(permissions: List<String>) {
        timeOfRequest = System.currentTimeMillis()
        callbacks.permissionRequest(permissions.filterNot(::isGranted))
    }

    private fun showRationale(onGranted: () -> Unit, onDenied: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.gps_permission_rationale_title))
            .setMessage(activity.getString(R.string.gps_permission_rationale_content))
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                onGranted()
                dialog.dismiss()
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, _ ->
                onDenied()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDenied(onGranted: () -> Unit, onDenied: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.gps_permission_denied_title))
            .setMessage(activity.getString(R.string.gps_permission_denied_content))
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                try {
                    activity.startActivity(activity.getApplicationSettingsIntent())
                    lifecycleOwner.observeReturn { onGranted() }
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e, "Asking permissions - Cannot open settings")
                    showDeniedWithoutCondition(onDenied = onDenied)
                }
            }
            .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, _ ->
                onDenied()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeniedWithoutCondition(onDenied: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.gps_permission_denied_title))
            .setMessage(activity.getString(R.string.gps_permission_cannot_open_settings_content))
            .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ ->
                onDenied()
                dialog.dismiss()
            }
            .create()
            .show()
    }

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
        if (wasProcessedLongEnoughForHuman()) {
            isGranted
                .filter { !it.value && !shouldShowRequestPermissionRationale(activity, it.key) }
                .keys
                .forEach(::setAsDeniedForever)
        }

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

    fun onEnablingGpsRequested(isGranted: ActivityResult) {
        if (isGranted.resultCode == Activity.RESULT_OK) {
            checkPermissionsAgain()
        } else {
            notifyFailed()
        }
    }

    private fun wasProcessedLongEnoughForHuman(): Boolean {
        val requestProcessingTime = System.currentTimeMillis() - timeOfRequest
        return requestProcessingTime > 300L
    }

    private class Callback(
        val permissionRequest: (List<String>) -> Unit,
        val resolutionRequest: (PendingIntent) -> Unit,
        val onPermission: () -> Unit,
        val onFailed: () -> Unit,
    )

    private companion object {
        val neededPermissions = listOfNotNull(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
        )
        val bonusPermissions = listOfNotNull(
            if (SDK_INT >= VERSION_CODES.Q) ACCESS_BACKGROUND_LOCATION else null
        )
        const val GPS_DENIED_FOREVER_KEY = "GPS_DENIED_FOREVER_KEY"
    }
}