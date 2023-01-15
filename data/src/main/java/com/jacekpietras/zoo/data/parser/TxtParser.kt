package com.jacekpietras.zoo.data.parser

import android.content.Context
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import androidx.annotation.RawRes
import com.jacekpietras.geometry.haversine
import com.jacekpietras.zoo.data.utils.cutOut
import com.jacekpietras.zoo.domain.feature.sensors.model.GpsHistoryEntity

@Suppress("unused")
internal class TxtParser(context: Context, @RawRes rawRes: Int) {

    @Suppress("MemberVisibilityCanBePrivate")
    val result: List<List<GpsHistoryEntity>>

    init {
        result = context.resources.openRawResource(rawRes)
            .bufferedReader()
            .readLines()
            .map { parseEntity(it) }
            .cutOut { prev, next ->
                next.timestamp - prev.timestamp < MINUTE_IN_MILLIS &&
                        haversine(prev.lon, prev.lat, next.lon, next.lat) < 20
            }
    }

    private fun parseEntity(input: String): GpsHistoryEntity =
        input
            .split(" ")
            .run {
                GpsHistoryEntity(
                    timestamp = get(0).toLong(),
                    lat = get(1).toDouble(),
                    lon = get(2).toDouble(),
                    accuracy = 0f,
                )
            }
}