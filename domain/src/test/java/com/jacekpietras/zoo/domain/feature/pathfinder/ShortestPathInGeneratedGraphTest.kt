package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.obsolete.ObsoleteGraphAnalyzer
import com.jacekpietras.zoo.domain.feature.vrp.algorithms.City
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AssertionFailureBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

    @Test
    fun `start outside graph`() = runTest {
        val roads = listOf(
            listOf(
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(0, 10),
                PointD(10, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )

        val result = roads.toGraph().getShortestPath(
            startPoint = PointD(9, 11),
            endPoint = PointD(0, 0),
            technicalAllowedAtStart = true,
            technicalAllowedAtEnd = true,
        )

        val expected = listOf(
            PointD(9, 10),
            PointD(10, 10),
            PointD(5, 5),
            PointD(0, 0),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `end outside graph`() = runTest {
        val roads = listOf(
            listOf(
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(0, 10),
                PointD(10, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )

        val result = roads.toGraph().getShortestPath(
            startPoint = PointD(0, 0),
            endPoint = PointD(9, 11),
            technicalAllowedAtStart = true,
            technicalAllowedAtEnd = true,
        )

        val expected = listOf(
            PointD(0, 0),
            PointD(5, 5),
            PointD(10, 10),
            PointD(9, 10),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `end outside graph 2`() = runTest {
        val roads = listOf(
            listOf(
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(0, 10),
                PointD(10, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )

        val result = roads.toGraph().getShortestPath(
            startPoint = PointD(0, 0),
            endPoint = PointD(1, 11),
            technicalAllowedAtStart = true,
            technicalAllowedAtEnd = true,
        )

        val expected = listOf(
            PointD(0, 0),
            PointD(0, 10),
            PointD(1, 10),
        )
        assertEquals(expected, result)
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

        private fun generateCity(
            random: Random,
        ): City =
            City(
                x = (random.nextDouble() * 360).toInt() - 180,
                y = (random.nextDouble() * 180).toInt() - 90,
            )

        internal fun generatePoint(
            random: Random,
        ): PointD =
            generateCity(random).let {
                PointD(
                    x = it.x.toDouble(),
                    y = it.y.toDouble(),
                )
            }

        internal fun generateGraph(
            random: Random,
            numberOfCities: Int,
            connections: Int,
        ): Pair<List<City>, List<List<PointD>>> {
            val points = MutableList(numberOfCities) { generateCity(random) }.toSet().toList()
            val c = points.associateWith { mutableListOf<City>() }

            val roads = (0 until connections).mapNotNull {
                val next = points.getRandom(random)
                points
                    .filter { it != next && c[next]?.contains(it) == false }
                    .minByOrNull { it.distanceToCity(next) }
                    ?.let { closest ->
                        c[next]!!.add(closest)
                        c[closest]!!.add(next)

                        listOf(
                            PointD(next.x, next.y),
                            PointD(closest.x, closest.y),
                        )
                    }
            }.toSet().toList()
            return points to roads
        }

        internal fun List<City>.getRandom(random: Random = Random(100)) =
            this[random.nextInt(lastIndex)]

        internal fun List<List<PointD>>.toGraph(): GraphAnalyzer =
            GraphAnalyzer().also { it.initialize(map(MapItemEntity::PathEntity), emptyList()) }

        internal fun List<List<PointD>>.toObsoleteGraph(): ObsoleteGraphAnalyzer =
            ObsoleteGraphAnalyzer().also { it.initialize(map(MapItemEntity::PathEntity), emptyList()) }

        internal fun List<PointD>.assertExistingRoute(roads: List<List<PointD>>) {
            zipWithNext { a, b ->
                val foundConnection = roads.find {
                    it.zipWithNext().any { (v1, v2) ->
                        a == v1 && b == v2 || a == v2 && b == v1
                    }
                }
                if (foundConnection == null) {
                    AssertionFailureBuilder.assertionFailure()
                        .message("Found path is not possible in graph")
                        .reason("Not found connection $a <-> $b")
                        .buildAndThrow()
                }
            }
        }

        internal fun assertIsNeighbour(point: PointD, node: PointD, roads: List<List<PointD>>) {
            roads.forEach {
                it.zipWithNext().forEach { (v1, v2) ->
                    if ((v1 == node || v2 == node) && isOnEdge(point, v1, v2)) {
                        return
                    }
                }
            }
            AssertionFailureBuilder.assertionFailure()
                .message("Found path is not possible in graph")
                .reason("Point (${point.x}, ${point.y}) is not in edge connected to (${node.x}, ${node.y})")
                .buildAndThrow()
        }

        private fun isOnEdge(point: PointD, edge1: PointD, edge2: PointD): Boolean =
            cartesian(edge1, point) + cartesian(edge2, point) - cartesian(edge1, edge2) < 0.000000001

        internal fun List<PointD>.distance(): Double =
            zipWithNext { a, b -> haversine(a, b) }.sum()
    }
}