package com.jacekpietras.zoo.data.utils

import android.content.Context
import android.os.Build
import com.facebook.device.yearclass.DeviceInfo

class PerformanceClass(context: Context) {

    private var cpuFreq = DeviceInfo.getCPUMaxFreqKHz() / MHZ_IN_KHZ
    private var numberOfCores = DeviceInfo.getNumberOfCPUCores()
    private var totalMemory = DeviceInfo.getTotalMemory(context).toDouble() / GB

    fun getRating(): Boolean {
        if (Build.BRAND == "Android") {
            return true // We have Emulator there
        }

        return numberOfCores >= 6 &&
                (cpuFreq < 5 || cpuFreq >= 1800) &&
                totalMemory >= 2
    }

    private companion object {

        const val GB = 1024L * 1024L * 1024L
        const val MHZ_IN_KHZ = 1000
    }
}