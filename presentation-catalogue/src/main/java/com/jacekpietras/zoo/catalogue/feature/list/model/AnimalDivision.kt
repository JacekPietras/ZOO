package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.annotation.DrawableRes
import com.jacekpietras.zoo.catalogue.R

enum class AnimalDivision(
     @DrawableRes val iconRes: Int,
) {
    MAMMAL(R.drawable.ic_lion),
    BIRD(R.drawable.ic_bird),
    AMPHIBIAN(R.drawable.ic_frog),
    REPTILE(R.drawable.ic_snake),
    FISH(R.drawable.ic_fish),
    ARTHROPOD(R.drawable.ic_spider),
    MOLLUSCA(R.drawable.ic_snail),
}