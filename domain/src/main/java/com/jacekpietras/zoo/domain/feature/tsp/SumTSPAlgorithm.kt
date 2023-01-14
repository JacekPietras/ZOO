package com.jacekpietras.zoo.domain.feature.tsp

internal class SumTSPAlgorithm<T : Any>(
    private val algorithm1: TSPWithFixedStagesAlgorithm<T>,
    private val algorithm2: TSPWithFixedStagesAlgorithm<T>,
) : TSPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
    ): List<T> {
        val tour = algorithm1
            .run(
                points = points,
                distanceCalculation = distanceCalculation,
                immutablePositions = immutablePositions,
            )
        return algorithm2
            .run(
                points = tour,
                distanceCalculation = distanceCalculation,
                immutablePositions = immutablePositions,
            )
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)
}

internal operator fun <T : Any> TSPWithFixedStagesAlgorithm<T>.plus(second: TSPWithFixedStagesAlgorithm<T>): TSPWithFixedStagesAlgorithm<T> {
    return SumTSPAlgorithm(this, second)
}