package com.jacekpietras.zoo.domain.feature.tsp

internal class DivorcedTSPAlgorithm<T : Any>(
    private val algorithm: TSPAlgorithm<T>,
    private val dummy: T, // todo think how to create new dummy inside
) : TSPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?
    ): Pair<Double, List<T>> {

        val tour = algorithm
            .run(
                points = points + dummy,
                distanceCalculation = { a, b ->
                    when {
                        a == dummy -> 0.0
                        b == dummy -> 0.0
                        else -> distanceCalculation(a, b)
                    }
                },
            )
            .second
        val connected = tour.shiftStartTo()
        val distance = connected.zipWithNext { a, b -> distanceCalculation(a, b) }.sum()

        return distance to connected
    }

    private fun List<T>.shiftStartTo(): List<T> {
        val indexOfDummy = indexOfFirst { it == dummy }
        val begin = subList(0, indexOfDummy)
        val end = subList(indexOfDummy + 1, lastIndex)
        return end + begin
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): Pair<Double, List<T>> =
        run(points, distanceCalculation, null)
}
