package com.jacekpietras.zoo.domain.feature.tsp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

internal class MultiplyTSPAlgorithm<T : Any>(
    private val algorithm1: TSPWithFixedStagesAlgorithm<T>,
    private val algorithm2: TSPWithFixedStagesAlgorithm<T>,
) : TSPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): List<T> = withContext(Dispatchers.Default) {
        val job1 = async {
            val tour1 = algorithm1
                .run(
                    points = points,
                    distanceCalculation = distanceCalculation,
                    immutablePositions = immutablePositions,
                )
            tour1 to distance(tour1, distanceCalculation)
        }

        val job2 = async {
            val tour2 = algorithm2
                .run(
                    points = points,
                    distanceCalculation = distanceCalculation,
                    immutablePositions = immutablePositions,
                )
            tour2 to distance(tour2, distanceCalculation)
        }

        if (job1.await().second < job2.await().second) {
            job1.getCompleted().first
        } else {
            job2.getCompleted().first
        }
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)

    private suspend fun distance(points: List<T>, distanceCalculation: suspend (T, T) -> Double) =
        points.zipWithNext { prev, next -> distanceCalculation(prev, next) }.sum()
}

internal operator fun <T : Any> TSPWithFixedStagesAlgorithm<T>.times(second: TSPWithFixedStagesAlgorithm<T>): TSPWithFixedStagesAlgorithm<T> {
    return MultiplyTSPAlgorithm(this, second)
}