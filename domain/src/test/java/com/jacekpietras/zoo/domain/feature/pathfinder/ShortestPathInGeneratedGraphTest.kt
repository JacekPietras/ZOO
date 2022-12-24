package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.tsp.algorithms.City
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

internal class ShortestPathInGeneratedGraphTest {

    @Test
    fun `find shortest path 1`() = runTest {
        doTest(
            seed = 1000,
            numberOfCities = 100,
            connections = 500,
            bestExpected = 536.0218658192133,
        )
    }

    @Test
    fun `find shortest path 2`() = runTest {
        doTest(
            seed = 2001,
            numberOfCities = 1000,
            connections = 2000,
            bestExpected = 517.6658074221267,
        )
    }

    @Test
    fun `find shortest path 3`() = runTest {
        doTest(
            seed = 3000,
            numberOfCities = 1000,
            connections = 2000,
            bestExpected = 551.1973440977065,
        )
    }

    @Test
    fun `find shortest path 4`() = runTest {
        doTest(
            seed = 4002,
            numberOfCities = 1000,
            connections = 3000,
            bestExpected = 279.78271215563814,
        )
    }

    @Test
    fun `find shortest path 5`() = runTest {
        doTest(
            seed = 5000,
            numberOfCities = 5000,
            connections = 10000,
            bestExpected = 445.50727166942147,
        )
    }

    private fun doTest(
        seed: Long,
        numberOfCities: Int,
        connections: Int,
        bestExpected: Double = 0.0,
    ) = runTest {
        val random = Random(seed)
        val (points, roads) = generateGraph(
            random,
            numberOfCities,
            connections,
        )
        val graphAnalyzer = roads.toGraph()
        val start = points.getRandom(random).let { PointD(it.x, it.y) }
        val end = points.getRandom(random).let { PointD(it.x, it.y) }

        val result = measureMap({ println("Calculated in $it") }) {
            graphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
                technicalAllowedAtStart = true,
                technicalAllowedAtEnd = true,
            )
        }
        val distance = result.distance()

        assertEquals(start, result.first())
        assertEquals(end, result.last())
        result.assertExistingRoute(roads)
        println("Path length: ${result.size}")

        if (distance < bestExpected) {
            println("New record: $distance")
        } else {
            assertEquals(bestExpected, distance)
        }
    }

    companion object {

        internal fun generateGraph(
            random: Random,
            numberOfCities: Int,
            connections: Int,
        ): Pair<List<City>, List<List<PointD>>> {
            val points = MutableList(numberOfCities) {
                City(
                    x = (random.nextDouble() * 500).toInt(),
                    y = (random.nextDouble() * 500).toInt(),
                )
            }.toSet().toList()
            val c = points.associateWith { mutableListOf<City>() }

            val roads = (0 until connections).map {
                val next = points.getRandom(random)
                val closest = points.filter { it != next && c[next]?.contains(it) == false }.minByOrNull { it.distanceToCity(next) }!!
                c[next]!!.add(closest)
                c[closest]!!.add(next)

                listOf(
                    PointD(next.x, next.y),
                    PointD(closest.x, closest.y),
                )
            }.toSet().toList()
            return points to roads
        }

        internal fun List<City>.getRandom(random: Random = Random(100)) =
            this[random.nextInt(lastIndex)]

        internal fun List<List<PointD>>.toGraph(): GraphAnalyzer =
            GraphAnalyzer().also { it.initialize(map(MapItemEntity::PathEntity), emptyList()) }

        internal fun List<PointD>.assertExistingRoute(roads: List<List<PointD>>) {
            zipWithNext { a, b ->
                val foundConnection = roads.find {
                    val v1 = it[0]
                    val v2 = it[1]

                    a == v1 && b == v2 || a == v2 && b == v1
                }
                assertNotNull(foundConnection) { "Not found connection $a <-> $b" }
            }
        }

        internal fun List<PointD>.distance(): Double =
            zipWithNext { a, b -> cartesian(a, b) }.sum()
    }
}