package com.jacekpietras.zoo.tracking.permissions

interface GpsPermissionRequester {

    fun checkPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit = {},
    )
}
