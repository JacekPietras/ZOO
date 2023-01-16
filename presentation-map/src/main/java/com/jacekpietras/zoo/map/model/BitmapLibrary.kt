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
                res = R.drawable.ic_tree_36,
                nightRes = R.drawable.ic_tree_36_night,
            ),
            "slonie" to BitmapVersions(
                context = context,
                res = R.drawable.ic_elephant_48,
            ),
            "wc-" to BitmapVersions(
                context = context,
                res = R.drawable.ic_wc_24,
            ),
            "wyjscie" to BitmapVersions(
                context = context,
                res = R.drawable.ic_door_24,
            ),
        )

    fun recycle() {
        data.forEach { it.value.recycle() }
    }
}