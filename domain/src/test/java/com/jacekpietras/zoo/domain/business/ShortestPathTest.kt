package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ShortestPathTest {

    @Nested
    @DisplayName("When Three point graph")
    inner class WhenThreePointGraph {

        private val snapshot: MutableSet<Node>

        //      [3]
        //       |
        // [1]--[2]

        init {
            val roads = listOf(
                MapItemEntity.PathEntity(
                    listOf(
                        PointD(1, 1),
                        PointD(2, 1),
                        PointD(2, 2),
                    ),
                )
            )
            GraphAnalyzer.initialize(roads, emptyList())
            snapshot = runBlocking { GraphAnalyzer.waitForNodes() }
        }

        @Test
        fun `find shortest path from ends of graph`() = runBlockingTest {
            val start = PointD(1, 1)
            val end = PointD(2, 2)

            val result = GraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )

            val expected = listOf(
                PointD(1, 1),
                PointD(2, 1),
                PointD(2, 2),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `find shortest path from end and middle of graph`() = runBlockingTest {
            val start = PointD(1, 1)
            val end = PointD(2, 1)

            val result = GraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )

            val expected = listOf(
                PointD(1, 1),
                PointD(2, 1),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `find shortest path from end and outside of graph`() = runBlockingTest {
            val start = PointD(1, 1)
            val end = PointD(2, 3)

            val result = GraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )

            val expected = listOf(
                PointD(1, 1),
                PointD(2, 1),
                PointD(2, 2),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `find shortest path from middle of paths`() = runBlockingTest {
            val start = PointD(1.5, 1.0)
            val end = PointD(2.0, 1.5)

            val result = GraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )

            val expected = listOf(
                PointD(1.5, 1.0),
                PointD(2, 1),
                PointD(2.0, 1.5),
            )
            assertEquals(expected, result)
        }

        @Test
        fun `find shortest path inside edge`() = runBlockingTest {
            val start = PointD(1.2, 1.0)
            val end = PointD(1.8, 1.0)

            val result = GraphAnalyzer.getShortestPath(
                endPoint = end,
                startPoint = start,
            )

            val expected = listOf(
                PointD(1.2, 1.0),
                PointD(1.8, 1.0),
            )
            assertEquals(expected, result)
        }

        @AfterEach
        fun `verify graph nodes not changed`() = runBlockingTest {
            val p1 = PointD(1, 1)
            val p2 = PointD(2, 1)
            val p3 = PointD(2, 2)
            val nodes = GraphAnalyzer.waitForNodes().toList()

            assertEquals(setOf(p1, p2, p3), nodes.map { it.point }.toSet(), "Cleanup failed")
            assertEquals(setOf(p2), nodes[0].edges.map { it.node.point }.toSet(), "Cleanup failed")
            assertEquals(setOf(p1, p3), nodes[1].edges.map { it.node.point }.toSet(), "Cleanup failed")
            assertEquals(setOf(p2), nodes[2].edges.map { it.node.point }.toSet(), "Cleanup failed")
            assertEquals(snapshot.toList(), nodes, "Cleanup failed")
        }
    }
}