package com.jacekpietras.zoo.core

import com.jacekpietras.zoo.core.text.Text

class RegionMapper {

    // todo map regions to string res
    fun from(regionId: String): Text =
        Text(regionId)
}
