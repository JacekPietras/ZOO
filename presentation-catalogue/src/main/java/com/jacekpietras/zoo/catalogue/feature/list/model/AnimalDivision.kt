package com.jacekpietras.zoo.catalogue.feature.list.model

import androidx.annotation.DrawableRes
import com.jacekpietras.zoo.catalogue.R

enum class AnimalDivision(
     @DrawableRes val iconRes: Int,
) {
    MAMMAL(R.drawable.ic_animal_lion_24dp),
    BIRD(R.drawable.ic_animal_bird_24dp),
    AMPHIBIAN(R.drawable.ic_animal_frog_24dp),
    REPTILE(R.drawable.ic_animal_snake_24dp),
    FISH(R.drawable.ic_animal_fish_24dp),
    ARTHROPOD(R.drawable.ic_animal_spider_24dp),
    MOLLUSCA(R.drawable.ic_animal_snail_24dp),
}