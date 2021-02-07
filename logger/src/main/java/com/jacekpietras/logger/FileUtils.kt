package com.jacekpietras.logger

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

@SuppressLint("LogNotTimber")
internal fun File.mkdirAndLog(): Boolean {
    if (!exists()) {
        if (!mkdirs()) {
            Log.e(
                "Utils", "directory can't be created: " + this
                        + ", SD Card available? " + if (isSdCardAvailable()) "Yes" else "No"
                        + ", Storage permissions? " + if (storagePermitted()) "Yes" else "No"
            )
            return false
        }
    }
    return true
}

private fun isSdCardAvailable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

private fun storagePermitted(): Boolean {
    val context: Context = DebugUtilsContextHolder.context

    val readPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
    val writePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
    return readPermission && writePermission
}
