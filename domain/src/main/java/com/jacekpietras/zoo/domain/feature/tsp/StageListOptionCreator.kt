package com.jacekpietras.zoo.domain.feature.tsp

import com.jacekpietras.zoo.domain.feature.planner.model.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

internal class StageListOptionCreator {

    suspend fun run(toCheck: List<Stage>, onResult: suspend (List<Stage>) -> Unit, checked: List<Stage> = emptyList()) {
        withContext(Dispatchers.Default) {
            if (isActive) {
                val problematicIndex = toCheck.indexOfFirstOrNull { it is Stage.Multiple && it.alternatives.size > 1 }
                if (problematicIndex != null) {
                    val beforeProblematic = toCheck.subList(0, problematicIndex)
                    val afterProblematic = toCheck.subList(problematicIndex + 1, toCheck.size)
                    val problematicStage = toCheck[problematicIndex] as Stage.Multiple
                    problematicStage.alternatives.forEach { alternativeRegion ->
                        val stageVariation = problematicStage.copy(region = alternativeRegion)
                        if (isActive) {
                            run(
                                checked = checked + beforeProblematic + stageVariation,
                                toCheck = afterProblematic,
                                onResult = onResult,
                            )
                        }
                    }
                } else {
                    onResult(checked + toCheck)
                }
            }
        }
    }

    private fun <E> List<E>.indexOfFirstOrNull(function: (E) -> Boolean): Int? =
        indexOfFirst(function).takeIf { it != -1 }
}