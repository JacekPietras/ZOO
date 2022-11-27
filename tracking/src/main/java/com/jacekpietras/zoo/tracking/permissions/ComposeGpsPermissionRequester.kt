package com.jacekpietras.zoo.tracking.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.jacekpietras.zoo.tracking.utils.getActivity

@Composable
fun rememberGpsPermissionRequesterState(): GpsPermissionRequester {
    val activity = LocalContext.current.getActivity()
    val lifecycleOwner = LocalLifecycleOwner.current

    val logic = remember {
        GpsPermissionRequesterLogic(
            activity = activity,
            lifecycleOwner = lifecycleOwner,
        )
    }
    
    val permissionResult = rememberLauncherForActivityResult(RequestMultiplePermissions(), logic::onPermissionsRequested)
    val enableGpsRequest = rememberLauncherForActivityResult(StartIntentSenderForResult(), logic::onEnablingGpsRequested)

    return object : GpsPermissionRequester {

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
}
