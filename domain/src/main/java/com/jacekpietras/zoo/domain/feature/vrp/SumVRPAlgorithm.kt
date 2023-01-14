package com.jacekpietras.zoo.domain.feature.vrp

internal class SumVRPAlgorithm<T : Any>(
    private val algorithm1: VRPWithFixedStagesAlgorithm<T>,
    private val algorithm2: VRPWithFixedStagesAlgorithm<T>,
) : VRPWithFixedStagesAlgorithm<T> {

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

internal operator fun <T : Any> VRPWithFixedStagesAlgorithm<T>.plus(second: VRPWithFixedStagesAlgorithm<T>): VRPWithFixedStagesAlgorithm<T> {
    return SumVRPAlgorithm(this, second)
}