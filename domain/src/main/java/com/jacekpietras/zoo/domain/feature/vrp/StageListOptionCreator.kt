package com.jacekpietras.zoo.domain.feature.vrp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import com.jacekpietras.zoo.domain.model.Region
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal class StageListOptionCreator {

    suspend fun run(toCheck: List<Stage>, onOptionFound: suspend (List<Stage>) -> Unit, checked: List<Stage> = emptyList()) {
        onBackground {
            val problematicIndex = toCheck.indexOfFirstMultipleOrNull()
            if (problematicIndex != null) {
                val beforeProblematic = toCheck.subList(0, problematicIndex)
                val problematicStage = toCheck[problematicIndex] as Stage.Multiple
                val afterProblematic = toCheck.subList(problematicIndex + 1, toCheck.size)

                val foundAlternative = problematicStage.alternatives
                    .find { alt -> checked.haveAnyRegion(alt) || toCheck.haveSingleRegion(alt) }

                if (foundAlternative != null) {
                    val stageVariation = problematicStage.copy(region = foundAlternative)
                    run(
                        checked = checked + beforeProblematic + stageVariation,
                        toCheck = afterProblematic,
                        onOptionFound = onOptionFound,
                    )
                } else {
                    problematicStage.alternatives.forEach { alternativeRegion ->
                        val stageVariation = problematicStage.copy(region = alternativeRegion)
                        run(
                            checked = checked + beforeProblematic + stageVariation,
                            toCheck = afterProblematic,
                            onOptionFound = onOptionFound,
                        )
                    }
                }
            } else {
                onOptionFound(checked + toCheck)
            }
        }
    }

    private fun List<Stage>.indexOfFirstMultipleOrNull(): Int? =
        indexOfFirstOrNull { it is Stage.Multiple && it.alternatives.size > 1 }

    private inline fun <E> List<E>.indexOfFirstOrNull(function: (E) -> Boolean): Int? =
        indexOfFirst(function).takeIf { it != -1 }

    private fun List<Stage>.haveSingleRegion(alt: Region): Boolean =
        any { it is Stage.Single && it.region == alt }

    private fun List<Stage>.haveAnyRegion(alt: Region): Boolean =
        any { it is Stage.InRegion && it.region == alt }

    private suspend inline fun onBackground(crossinline block: suspend () -> Unit) {
        withContext(Dispatchers.Default) {
            if (isActive) {
                block()
            }
        }
    }
}