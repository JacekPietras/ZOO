package com.jacekpietras.mapview.utils

import android.os.Build

fun isProbablyRunningOnEmulator(): Boolean =
    // Android SDK emulator
    ((Build.FINGERPRINT.startsWith("google/sdk_gphone_") || Build.FINGERPRINT.startsWith("Android/sdk_gphone_"))
            && Build.FINGERPRINT.endsWith(":user/release-keys")
            && (Build.MANUFACTURER == "Google" || Build.MANUFACTURER == "unknown")
            && Build.PRODUCT.startsWith("sdk_gphone_")
            && (Build.BRAND.lowercase() == "google" || Build.BRAND.lowercase() == "android")
            && (Build.MODEL.startsWith("sdk_gphone_") || Build.MODEL.startsWith("Android SDK")))
            //
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            // Bluestacks
            || "QC_Reference_Phone" == Build.BOARD && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HOST.startsWith("Build")
            //MSI App Player
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.PRODUCT == "google_sdk"
