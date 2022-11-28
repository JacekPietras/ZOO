package com.jacekpietras.zoo.data.utils

import android.content.Context
import com.facebook.device.yearclass.DeviceInfo
import timber.log.Timber

class PerformanceClass(context: Context) {

    private var cpuFreq = DeviceInfo.getCPUMaxFreqKHz()
    private var numberOfCores = DeviceInfo.getNumberOfCPUCores()
    private var totalMemory = DeviceInfo.getTotalMemory(context).toDouble() / GB

    fun getRating(): Boolean {
        Timber.e("dups cpuFreq $cpuFreq")
        Timber.e("dups numberOfCores $numberOfCores")
        Timber.e("dups totalMemory $totalMemory")

        return numberOfCores >= 4 && (cpuFreq == -1 || cpuFreq >= 2) && totalMemory >= 1.5
    }

    private companion object {

        const val GB = 1024L * 1024L * 1024L
    }
}