package com.jacekpietras.mapview.ui

import android.text.format.DateUtils
import com.jacekpietras.mapview.BuildConfig

object LastMapUpdate {

    var lastTransform: Long = 0L
    var cutoStart: Long = 0L
    var cutoEnd: Long = 0L
    var renderStart: Long = 0L

    var lastUpdate: Long = 0L
    val fpsList = mutableListOf<Long>()
    val medFps get() = fpsList.average().toLong()

    fun update() {
        if (BuildConfig.DEBUG) {
            val timeFromLast = (System.currentTimeMillis() - lastUpdate)
            if (timeFromLast > 0) {
                val fps = DateUtils.SECOND_IN_MILLIS / timeFromLast
                if (fps in (0..200)) {
                    fpsList.add(fps)
                    if (fpsList.size > 20) {
                        fpsList.removeAt(0)
                    }
                }
            }
            lastUpdate = System.currentTimeMillis()
        }
    }
}