package com.jacekpietras.zoo.data.gps

import android.content.Context
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import androidx.annotation.RawRes
import com.jacekpietras.core.cutOut
import com.jacekpietras.core.haversine
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity

internal class TxtParser(context: Context, @RawRes rawRes: Int) {

    val result: List<List<GpsHistoryEntity>>

    init {
        context.resources.openRawResource(rawRes)
            .bufferedReader()
            .use { reader ->
                result = reader
                    .readLines()
                    .map { parseEntity(it) }
                    .cutOut { prev, next ->
                        next.timestamp - prev.timestamp < MINUTE_IN_MILLIS &&
                                haversine(prev.lon, prev.lat, next.lon, next.lat) < 20
                    }
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
                )
            }
}