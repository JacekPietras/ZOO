package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.assertExistingRoute
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.distance
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.getRandom
import com.jacekpietras.zoo.domain.feature.pathfinder.ShortestPathInGeneratedGraphTest.Companion.toGraph
import com.jacekpietras.zoo.domain.utils.measureMap
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class MinGraphAnalyzerTest {

    @Test
    fun `map to min graph`() = runTest{
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
        )

        val minGraph = roads.toGraph().toMinGraph()

        val resultNodes = minGraph.waitForNodes()
    }

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
        val (points, roads) = ShortestPathInGeneratedGraphTest.generateGraph(
            random,
            numberOfCities,
            connections,
        )
        val minGraphAnalyzer = roads.toGraph().toMinGraph()
        val start = points.getRandom(random).let { PointD(it.x, it.y) }
        val end = points.getRandom(random).let { PointD(it.x, it.y) }

        val result = measureMap({ println("Calculated in $it") }) {
            minGraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
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

    private fun GraphAnalyzer.toMinGraph(): MinGraphAnalyzer = runBlocking {
        MinGraphAnalyzer().also { it.initialize(waitForNodes()) }
    }
}