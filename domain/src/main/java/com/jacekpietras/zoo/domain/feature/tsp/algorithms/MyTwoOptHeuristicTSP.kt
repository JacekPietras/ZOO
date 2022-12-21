package com.jacekpietras.zoo.domain.feature.tsp.algorithms

import com.jacekpietras.zoo.domain.feature.tsp.TSPWithFixedStagesAlgorithm
import com.jacekpietras.zoo.domain.utils.forEachPairIndexed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class MyTwoOptHeuristicTSP<T : Any> : TSPWithFixedStagesAlgorithm<T> {

    override suspend fun run(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?
    ): List<T> {
        return withContext(Dispatchers.Default) {
            runInner(
                points,
                distanceCalculation,
                immutablePositions,
                stopCondition = { !isActive },
            )
        }
    }

    private suspend fun runInner(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double,
        immutablePositions: List<Int>?,
        stopCondition: () -> Boolean,
    ): List<T> {
        val n = points.size
        val dist = createWeightArray(points, distanceCalculation)
        val tour = IntArray(n + 2) { it }
        tour[n + 1] = 0

        while (true) {
            var minChange = -minCostImprovement
            var mini = -1
            var minj = -1
            for (i in 0 until n - 1) {
                for (j in i + 2 until n + 1) {
                    val ci = tour[i]
                    val ci1 = tour[i + 1]
                    val cj = tour[j]
                    val cj1 = tour[j + 1]
                    val change = dist[ci][cj] + dist[ci1][cj1] - dist[ci][ci1] - dist[cj][cj1]
                    if (change < minChange) {
                        if (immutablePositions == null || immutablePositions.allows(i + 1, j, n)) {
                            minChange = change
                            mini = i
                            minj = j
                        }
                    }
                }
            }
            if (mini == -1 || minj == -1 || stopCondition()) {
                return toPointList(tour, points)
            }
            reverse(tour, mini + 1, minj)
        }
    }

    override suspend fun run(points: List<T>, distanceCalculation: suspend (T, T) -> Double): List<T> =
        run(points, distanceCalculation, null)

    private fun List<Int>.allows(i: Int, j: Int, n: Int): Boolean {
        var x = (i..j).toList()
        val isOdd = x.size % 2 != 0
        if (isOdd) {
            x = x - x[x.size / 2]
        }
        forEach {
            if (x.contains(it)) return false
        }
        if (x.contains(n)) return false
        return true
    }

    private suspend fun createWeightArray(
        points: List<T>,
        distanceCalculation: suspend (T, T) -> Double
    ): Array<DoubleArray> {
        val n = points.size
        val dist = Array(n + 1) { DoubleArray(n + 1) { 0.0 } }

        points.forEachPairIndexed { si, s, ti, t ->
            val weight = distanceCalculation(s, t)
            dist[si][ti] = weight
            dist[ti][si] = weight
        }
        return dist
    }

    private fun reverse(arr: IntArray, from: Int, to: Int) {
        var i = from
        var j = to
        while (i < j) {
            val tmp = arr[j]
            arr[j] = arr[i]
            arr[i] = tmp
            ++i
            --j
        }
    }

    private fun <T> toPointList(arr: IntArray, points: List<T>): List<T> {
        val n = arr.size - 2
        val shift = arr.indexOf(n)
        return List(n) { points[arr[(it + shift + 1) % (n + 1)]] }
    }

    private companion object {

        const val minCostImprovement = 1.0E-8
    }
}
