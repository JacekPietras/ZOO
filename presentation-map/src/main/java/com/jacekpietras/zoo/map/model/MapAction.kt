package com.jacekpietras.zoo.map.model

import androidx.annotation.StringRes
import com.jacekpietras.zoo.map.R

enum class MapAction(
    @StringRes val title: Int,
) {
    AROUND_YOU(R.string.around_you),
    WC(R.string.wc),
    RESTAURANT(R.string.restaurant),
    EXIT(R.string.exit),
}