package com.jacekpietras.zoo.map.model

import android.content.Context
import com.jacekpietras.zoo.map.R

class BitmapLibrary(
    context: Context,
) {

    val data: Map<String, BitmapVersions> =
        mapOf(
            "tree" to BitmapVersions(
                context = context,
                res = R.drawable.ic_map_tree_36,
                nightRes = R.drawable.ic_map_tree_36_night,
            ),
            "slonie" to BitmapVersions(
                context = context,
                res = R.drawable.ic_map_elephant_64,
            ),
            "wielkie-koty-2" to BitmapVersions(
                context = context,
                res = R.drawable.ic_map_lion_64,
            ),
            "wc-" to BitmapVersions(
                context = context,
                res = R.drawable.ic_map_wc_24,
                nightRes = R.drawable.ic_map_wc_24_night,
            ),
            "wyjscie" to BitmapVersions(
                context = context,
                res = R.drawable.ic_map_door_24,
                nightRes = R.drawable.ic_map_door_24_night,
            ),
        )

    fun recycle() {
        data.forEach { it.value.recycle() }
    }
}