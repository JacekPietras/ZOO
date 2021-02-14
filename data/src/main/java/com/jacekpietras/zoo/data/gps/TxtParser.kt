package com.jacekpietras.zoo.data.gps

import android.content.Context
import androidx.annotation.RawRes
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity

internal class TxtParser(context: Context, @RawRes rawRes: Int) {

    val result: List<GpsHistoryEntity>

    init {
        context.resources.openRawResource(rawRes)
            .bufferedReader()
            .use { reader ->
                result = reader
                    .readLines()
                    .map { parseEntity(it) }
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