package com.jacekpietras.zoo.tracking.permissions

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.jacekpietras.zoo.tracking.utils.getActivity

@Composable
fun ComposeGpsPermissionRequester(): GpsPermissionRequester {
    val logic = GpsPermissionRequesterLogic(activity = LocalContext.current.getActivity(), lifecycleOwner = LocalLifecycleOwner.current)
    val permissionResult = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
        if (isGranted.filter { it.value }.isNotEmpty()) logic.checkPermissionsAgain()
        else logic.notifyFailed()
    }
    val resolutionResult = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { isGranted ->
        if (isGranted.resultCode == Activity.RESULT_OK) logic.checkPermissionsAgain()
        else logic.notifyFailed()
    }

    return object : GpsPermissionRequester {

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
}
