package com.jacekpietras.zoo.catalogue.feature.list.mapper

import com.jacekpietras.zoo.catalogue.feature.list.model.AnimalDivision
import com.jacekpietras.zoo.domain.model.Division

internal class DivisionMapper {

    fun from(division: AnimalDivision): Division = Division.valueOf(division.name)

    fun from(division: Division): AnimalDivision = AnimalDivision.valueOf(division.name)
}
