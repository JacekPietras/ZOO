package com.jacekpietras.zoo.tracking.permissions

import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.fragment.app.Fragment
import com.jacekpietras.zoo.tracking.utils.getAppCompatActivity

@Suppress("unused")
class LegacyGpsPermissionRequester(fragment: Fragment) : GpsPermissionRequester {

    private var activity = fragment.requireActivity().getAppCompatActivity()
    private val logic = GpsPermissionRequesterLogic(
        activity = activity,
        lifecycleOwner = activity,
    )
    private val permissionResult = fragment.registerForActivityResult(RequestMultiplePermissions(), logic::onPermissionsRequested)
    private val enableGpsRequest = fragment.registerForActivityResult(StartIntentSenderForResult(), logic::onEnablingGpsRequested)

    override fun checkPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        logic.checkPermissions(
            permissionRequest = { permissions -> permissionResult.launch(permissions.toTypedArray()) },
            enableGpsRequest = { intent -> enableGpsRequest.launch(IntentSenderRequest.Builder(intent).build()) },
            onGranted = onGranted,
            onDenied = onDenied,
        )
    }
}
