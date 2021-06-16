package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.annotation.DrawableRes
import com.jacekpietras.zoo.catalogue.R

enum class AnimalDivision(
     @DrawableRes val iconRes: Int,
) {
    MAMMAL(R.drawable.ic_animal_lion_24),
    BIRD(R.drawable.ic_animal_bird_24),
    AMPHIBIAN(R.drawable.ic_animal_frog_24),
    REPTILE(R.drawable.ic_animal_snake_24),
    FISH(R.drawable.ic_animal_fish_24),
    ARTHROPOD(R.drawable.ic_animal_spider_24),
    MOLLUSCA(R.drawable.ic_animal_snail_24),
}