package com.jacekpietras.zoo.tracking.permissions

import android.app.Activity.RESULT_OK
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
    private val permissionResult =
        fragment.registerForActivityResult(RequestMultiplePermissions()) { isGranted ->
            if (isGranted.filter { it.value }.isNotEmpty()) logic.checkPermissionsAgain()
            else logic.notifyFailed()
        }
    private val resolutionResult =
        fragment.registerForActivityResult(StartIntentSenderForResult()) { isGranted ->
            if (isGranted.resultCode == RESULT_OK) logic.checkPermissionsAgain()
            else logic.notifyFailed()
        }

    override fun checkPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        logic.checkPermissions(
            permissionRequest = { permissions -> permissionResult.launch(permissions.toTypedArray()) },
            resolutionRequest = { intent -> resolutionResult.launch(IntentSenderRequest.Builder(intent).build()) },
            onGranted = onGranted,
            onDenied = onDenied,
        )
    }
}
