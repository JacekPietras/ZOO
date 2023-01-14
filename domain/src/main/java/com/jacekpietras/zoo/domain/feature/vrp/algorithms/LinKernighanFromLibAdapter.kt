package com.jacekpietras.zoo.domain.feature.vrp.algorithms

import com.jacekpietras.zoo.domain.feature.vrp.VRPWithFixedStagesAlgorithm
import com.jacekpietras.zoo.domain.utils.forEachPairIndexed

class LinKernighanFromLibAdapter<T : Any> : VRPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?
    ): List<T> {
        val alg = LinKernighanFromLib(
            points.size,
            IntArray(points.size + 2) { it }.also { it[points.size + 1] = 0 },
            createWeightArray(points, distanceCalculation),
        )
        alg.runAlgorithm()

        return toPointList(alg.tour, points)
    }

    private fun toPointList(tour: IntArray, points: List<T>): List<T> {
        val n = tour.size - 2
        val shift = tour.indexOf(n)
        return List(n) { points[tour[(it + shift + 1) % (n + 1)]] }
    }

    private suspend fun createWeightArray(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): Array<DoubleArray> {
//        val res = Array(points.size) { DoubleArray(points.size) }
//
//        for (i in 0 until points.size - 1) {
//            for (j in i + 1 until points.size) {
//                val p1 = points[i]
//                val p2 = points[j]
//                res[i][j] = distanceCalculation(p1, p2)
//                res[j][i] = res[i][j]
//            }
//        }
//        return res

        val n = points.size
        val dist = Array(n + 1) { DoubleArray(n + 1) { 0.0 } }

        points.forEachPairIndexed { si, s, ti, t ->
            val weight = distanceCalculation(s, t)
            dist[si][ti] = weight
            dist[ti][si] = weight
        }
        return dist
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)
}
