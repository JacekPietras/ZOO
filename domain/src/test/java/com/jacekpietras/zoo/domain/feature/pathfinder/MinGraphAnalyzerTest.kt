package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.assertExistingRoute
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.distance
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.generateGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.getRandom
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.toGraph
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinEdge
import com.jacekpietras.zoo.domain.feature.pathfinder.model.MinNode
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.time.Duration

internal class MinGraphAnalyzerTest {

    @Test
    fun `map to min graph`() = runTest {
        val roads = listOf(
            listOf(
                PointD(1, 1),
                PointD(2, 1),
                PointD(2, 2),
                PointD(3, 3),
            ),
            listOf(
                PointD(2, 2),
                PointD(3, 2),
            ),
            listOf(
                PointD(3, 2),
                PointD(4, 2),
                PointD(5, 2),
                PointD(6, 2),
                PointD(7, 2),
            ),
        )

        val minGraph = roads.toGraph().toMinGraph()
        val resultNodes = minGraph.waitForNodes()

        val expectedTerminalNodes =
            setOf(
                PointD(1, 1),
                PointD(2, 2),
                PointD(3, 3),
                PointD(7, 2),
            )
        assertEquals(expectedTerminalNodes, resultNodes.map(MinNode::point).toSet())
        val expectedCornerNodes =
            setOf(
                PointD(2, 1),
                PointD(3, 2),
                PointD(4, 2),
                PointD(5, 2),
                PointD(6, 2),
            )
        val resultCornerNodes = resultNodes.map { it.edges.cornerPoints() }.flatten().flatten().toSet()
        assertEquals(expectedCornerNodes, resultCornerNodes)
        assertEquals(4, resultNodes.size)
    }

    private fun Collection<MinEdge>.cornerPoints() =
        map { it.corners.map(Pair<PointD, Double>::first) }

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
        val startPoint = PointD(9, 11)
        val endPoint = PointD(0, 0)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(9, 10),
            PointD(10, 10),
            PointD(5, 5),
            PointD(0, 0),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `start outside graph 2`() = runTest {
        val roads = listOf(
            listOf(
                PointD(-1, -1),
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(-1, 10),
                PointD(0, 10),
                PointD(10, 10),
                PointD(11, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )
        val startPoint = PointD(9, 11)
        val endPoint = PointD(0, 0)

        val result = runShortestPath(roads, startPoint, endPoint)

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
        val startPoint = PointD(0, 0)
        val endPoint = PointD(9, 11)

        val result = runShortestPath(roads, startPoint, endPoint)

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
        val startPoint = PointD(0, 0)
        val endPoint = PointD(1, 10)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(0, 0),
            PointD(0, 10),
            PointD(1, 10),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `end outside graph 3`() = runTest {
        val roads = listOf(
            listOf(
                PointD(-1, -1),
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(-1, 10),
                PointD(0, 10),
                PointD(10, 10),
                PointD(11, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )
        val startPoint = PointD(0, 0)
        val endPoint = PointD(9, 11)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(0, 0),
            PointD(5, 5),
            PointD(10, 10),
            PointD(9, 10),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `end outside graph 4`() = runTest {
        val roads = listOf(
            listOf(
                PointD(-1, -1),
                PointD(0, 0),
                PointD(0, 10),
            ),
            listOf(
                PointD(0, 0),
                PointD(5, 5),
                PointD(10, 10),
            ),
            listOf(
                PointD(-1, 10),
                PointD(0, 10),
                PointD(10, 10),
                PointD(11, 10),
            ),
            listOf(
                PointD(5, 5),
                PointD(5, 6),
            ),
        )
        val startPoint = PointD(0, 0)
        val endPoint = PointD(1, 11)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(0, 0),
            PointD(0, 10),
            PointD(1, 10),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `on straight line`() = runTest {
        val roads = listOf(
            listOf(
                PointD(0, 0),
                PointD(0, 5),
                PointD(0, 10),
            ),
        )
        val startPoint = PointD(1, 2)
        val endPoint = PointD(1, 8)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(0, 2),
            PointD(0, 5),
            PointD(0, 8),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `on straight line, close`() = runTest {
        val roads = listOf(
            listOf(
                PointD(0, 0),
                PointD(0, 5),
                PointD(0, 10),
            ),
        )
        val startPoint = PointD(1, 2)
        val endPoint = PointD(1, 3)

        val result = runShortestPath(roads, startPoint, endPoint)

        val expected = listOf(
            PointD(0, 2),
            PointD(0, 3),
        )
        assertEquals(expected, result)
    }

    private suspend fun runShortestPath(
        roads: List<List<PointD>>,
        startPoint: PointD,
        endPoint: PointD
    ): List<PointD> {
        val fullGraphAnalyzer = roads.toGraph()
        val minGraphAnalyzer = fullGraphAnalyzer.toMinGraph()
        var fullResultTime = Duration.ZERO
        val fullResult = measureMap({ fullResultTime = it }) {
            fullGraphAnalyzer.getShortestPath(
                startPoint = startPoint,
                endPoint = endPoint,
                technicalAllowedAtStart = true,
                technicalAllowedAtEnd = true,
            )
        }
        var resultTime = Duration.ZERO
        val result = measureMap({ resultTime = it }) {
            minGraphAnalyzer.getShortestPath(
                startPoint = startPoint,
                endPoint = endPoint,
                technicalAllowedAtStart = true,
                technicalAllowedAtEnd = true,
            )
        }
        if (resultTime < fullResultTime) {
            println("Calculated in $resultTime, (${fullResultTime - resultTime} faster)")
        } else {
            println("Calculated in $resultTime, (${resultTime - fullResultTime} slower!)")
        }

        assertEquals(fullResult, result) {
            "Result from Full Graph is different\n" +
                    "Full distance:${fullResult.distance()}, Min distance ${result.distance()}\n" +
                    "Full length: ${fullResult.size}, Min length: ${result.size}\n"
        }
        return result
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
        val start = points.getRandom(random).let { PointD(it.x, it.y) }
        val end = points.getRandom(random).let { PointD(it.x, it.y) }

        val result = runShortestPath(roads, start, end)

        println("Path length: ${result.size}")

        assertEquals(start, result.first())
        assertEquals(end, result.last())
        result.assertExistingRoute(roads)

        val distance = result.distance()
        if (distance < bestExpected) {
            println("New record: $distance")
        } else {
            assertEquals(bestExpected, distance)
        }
    }

    private fun GraphAnalyzer.toMinGraph(): MinGraphAnalyzer = runBlocking {
        MinGraphAnalyzer().also { it.initialize(waitForNodes()) }
    }
}