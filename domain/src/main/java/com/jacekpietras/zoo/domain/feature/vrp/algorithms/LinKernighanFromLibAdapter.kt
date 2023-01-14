package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.VRPWithFixedStagesAlgorithm

class LinKernighanFromLibAdapter<T : Any> : VRPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?
    ): List<T> {
        val alg = LinKernighanFromLib(
            points.size,
            IntArray(points.size) { it },
            createWeightArray(points, distanceCalculation),
        )
        alg.runAlgorithm()

        return toPointList(alg.tour, points)
    }

    private fun toPointList(tour: IntArray, points: List<T>): List<T> {
        val n = tour.size
        return List(n) { points[tour[it]] }
    }

    private suspend fun createWeightArray(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): Array<DoubleArray> {
        val res = Array(points.size) { DoubleArray(points.size) }

        for (i in 0 until points.size - 1) {
            for (j in i + 1 until points.size) {
                res[i][j] = distanceCalculation(points[i], points[j])
                res[j][i] = res[i][j]
            }
        }
        return res
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)
}
