package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage

internal class StageListOptionCreator {

    fun run(toCheck: List<Stage>, checked: List<Stage> = emptyList()): List<List<Stage>> {
        val problematicIndex = toCheck.indexOfFirstOrNull { it is Stage.Multiple }
        return if (problematicIndex != null) {
            val beforeProblematic = toCheck.subList(0, problematicIndex)
            val afterProblematic = toCheck.subList(problematicIndex + 1, toCheck.size)
            val problematicStage = toCheck[problematicIndex] as Stage.Multiple
            problematicStage.alternatives.map { alternativeRegionId ->
                val stageVariation = problematicStage.copy(regionId = alternativeRegionId)
                run(
                    checked = checked + beforeProblematic + stageVariation,
                    toCheck = afterProblematic,
                )
            }.flatten()
        } else {
            listOf(checked + toCheck)
        }
    }

    private fun <E> List<E>.indexOfFirstOrNull(function: (E) -> Boolean): Int? =
        indexOfFirst(function).takeIf { it != -1 }
}