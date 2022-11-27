package com.jacekpietras.zoo.tracking.permissions.checker

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat.checkSelfPermission

internal class AndroidPermissionChecker(
    private val context: Context,
    private val checkSelfPermission: (Context, String) -> Int = ::checkSelfPermission
) : PermissionChecker {

    override fun hasPermissions(permissions: List<String>): Boolean =
        permissions.all { permission ->
            checkSelfPermission(context, permission) == PERMISSION_GRANTED
        }
}
