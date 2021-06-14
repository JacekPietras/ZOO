package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.annotation.DrawableRes
import com.jacekpietras.zoo.catalogue.R

enum class AnimalDivision(
     @DrawableRes val iconRes: Int,
) {
    MAMMAL(R.drawable.ic_dickbutt_24dp),
    BIRD(R.drawable.ic_dickbutt_24dp),
    AMPHIBIAN(R.drawable.ic_dickbutt_24dp),
    REPTILE(R.drawable.ic_dickbutt_24dp),
    FISH(R.drawable.ic_dickbutt_24dp),
    ARTHROPOD(R.drawable.ic_dickbutt_24dp),
    MOLLUSCA(R.drawable.ic_dickbutt_24dp),
}