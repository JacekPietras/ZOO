package com.jacekpietras.zoo.tracking.permissions.checker

internal interface PermissionChecker {

    fun hasPermissions(permissions: List<String>): Boolean
}
