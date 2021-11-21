package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.Snapped
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object GraphAnalyzer {

    private var nodes: MutableSet<Node>? = null
    private val snapper = Snapper()
    private val mutex = Mutex()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes = Builder(roads, technical).build()
    }

    suspend fun getTerminalPoints(): List<PointD> =
        waitForNodes()
            .asSequence()
            .filter { it.edges.size > 2 }
            .map { it.point }
            .toList()

    suspend fun getSnapped(
        point: PointD,
        technicalAllowed: Boolean,
    ): PointD =
        snapper.getSnappedPoint(
            waitForNodes(),
            point,
            technicalAllowed
        )

    suspend fun getSnappedPointWithContext(
        point: PointD,
        technicalAllowed: Boolean,
    ): Snapped =
        snapper.getSnappedPointWithContext(
            waitForNodes(),
            point,
            technicalAllowed
        )

    suspend fun getShortestPathWithContext(
        endPoint: Snapped,
        startPoint: Snapped,
        technicalAllowed: Boolean = false,
        technicalBlocked: Boolean = false,
    ): List<Node> {
        mutex.withLock {
            val nodes = waitForNodes()

            val cleanupList: MutableList<Node> = mutableListOf()

            val start = when (startPoint.point) {
                startPoint.near1.point -> startPoint.near1
                startPoint.near2.point -> startPoint.near2
                else -> nodes.createAndConnect(startPoint)
                    .also { cleanupList.add(it) }
            }

            val end = when (endPoint.point) {
                endPoint.near1.point -> endPoint.near1
                endPoint.near2.point -> endPoint.near2
                else -> nodes.createAndConnect(endPoint)
                    .also { cleanupList.add(it) }
            }

            val result = Dijkstra(nodes, start, end, technicalAllowed = technicalAllowed, technicalBlocked = technicalBlocked).getPath()

            cleanupList.forEach { fake ->
                val edges = fake.edges.toList()
                if (edges.size > 2) throw IllegalStateException("more than two edges in fake node")

                val edge1 = edges[0]
                val edge2 = edges[1]

                edge1.node.disconnect(fake)
                edge2.node.disconnect(fake)
                nodes.remove(fake)
            }

            return result
        }
    }

    suspend fun getShortestPath(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowed: Boolean = false,
    ): List<PointD> {
        mutex.withLock {
            val nodes = waitForNodes()

            if (startPoint == null) return listOf(endPoint)
            if (nodes.isEmpty()) return listOf(endPoint)

            val snapStart = snapper.getSnappedEdge(nodes, startPoint, technicalAllowed = true)
            val snapEnd = snapper.getSnappedEdge(nodes, endPoint, technicalAllowed = technicalAllowed)

            val cleanupList: MutableList<Node> = mutableListOf()

            val start = when (snapStart.point) {
                snapStart.near1.point -> snapStart.near1
                snapStart.near2.point -> snapStart.near2
                else -> {
                    nodes.createAndConnect(snapStart)
                        .also { cleanupList.add(it) }
                }
            }

            val end = when (snapEnd.point) {
                snapEnd.near1.point -> snapEnd.near1
                snapEnd.near2.point -> snapEnd.near2
                else -> {
                    nodes.createAndConnect(snapEnd)
                        .also { cleanupList.add(it) }
                }
            }

            val result = Dijkstra(nodes, start, end, technicalAllowed = technicalAllowed).getPath()
                .map { PointD(it.x, it.y) }

            cleanupList.forEach { fake ->
                val edges = fake.edges.toList()
                if (edges.size > 2) throw IllegalStateException("more than two edges in fake node")

                val edge1 = edges[0]
                val edge2 = edges[1]

                edge1.node.disconnect(fake)
                edge2.node.disconnect(fake)
                nodes.remove(fake)
            }

            return result
        }
    }

    private suspend fun waitForNodes(): MutableSet<Node> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }

    private fun MutableSet<Node>.createAndConnect(snap: Snapped): Node {
        val node = Node(snap.point)
        val edge = snap.near1.edges.first { it.node == snap.near2 }

        snap.near1.connectAndCalc(node, edge.technical, backward = false)
        snap.near2.connectAndCalc(node, edge.technical, backward = true)
        node.connectAndCalc(snap.near1, edge.technical, backward = false)
        node.connectAndCalc(snap.near2, edge.technical, backward = true)
        this.add(node)

        return node
    }
}