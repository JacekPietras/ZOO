package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jacekpietras.zoo.catalogue.R

enum class AnimalDivision(
     @DrawableRes val iconRes: Int,
     @StringRes val nameRes:Int,
) {
    MAMMAL(R.drawable.ic_animal_lion_24, R.string.mammal),
    BIRD(R.drawable.ic_animal_bird_24, R.string.bird),
    AMPHIBIAN(R.drawable.ic_animal_frog_24, R.string.amphibian),
    REPTILE(R.drawable.ic_animal_snake_24, R.string.reptile),
    FISH(R.drawable.ic_animal_fish_24, R.string.fish),
    ARTHROPOD(R.drawable.ic_animal_spider_24, R.string.arthropod),
    MOLLUSK(R.drawable.ic_animal_snail_24, R.string.mollusk),
}