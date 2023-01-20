package com.jacekpietras.mapview.ui

import android.text.format.DateUtils
import com.jacekpietras.mapview.BuildConfig

object LastMapUpdate {

    // fixme remove them
    var trans: Long = 0L
    var cutoS: Long = 0L
    var moveE: Long = 0L
    var tranS: Long = 0L
    var cutoE: Long = 0L
    var rendS: Long = 0L
    var rendE: Long = 0L
    var sortS: Long = 0L
    var sortE: Long = 0L
    var mergE: Long = 0L

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