package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.SnappedOnEdge
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object GraphAnalyzer {

    private var nodes: MutableSet<Node>? = null
    private val snapper = PointSnapper()
    private val mutex = Mutex()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes = Builder(roads, technical).build()
    }

    fun isInitialized(): Boolean = nodes != null

    suspend fun getTerminalPoints(): List<PointD> =
        waitForNodes()
            .asSequence()
            .filter { it.edges.size > 2 }
            .map { it.point }
            .toList()

    suspend fun getSnappedPointOnEdge(
        point: PointD,
        technicalAllowed: Boolean,
    ): SnappedOnEdge =
        snapper.getSnappedOnEdge(
            waitForNodes(),
            point,
            technicalAllowed
        )

    suspend fun getShortestPathWithContext(
        endPoint: SnappedOnEdge,
        startPoint: SnappedOnEdge,
        technicalAllowed: Boolean = false,
    ): List<Node> {
        mutex.withLock {

            return getShortestPathJob(
                endPoint = endPoint,
                startPoint = startPoint,
                technicalAllowed = technicalAllowed,
            )
        }
    }

    suspend fun getShortestPathJob(
        endPoint: SnappedOnEdge,
        startPoint: SnappedOnEdge,
        technicalAllowed: Boolean = false,
    ): List<Node> {
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

        val result = Dijkstra(nodes, start, end, technicalAllowed = technicalAllowed).getPath()

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

    suspend fun getShortestPath(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowed: Boolean = false,
    ): List<PointD> {
        mutex.withLock {
            val nodes = waitForNodes()

            if (startPoint == null) return listOf(endPoint)
            if (nodes.isEmpty()) return listOf(endPoint)

            val snapStart = snapper.getSnappedOnEdge(nodes, startPoint, technicalAllowed = true)
            val snapEnd = snapper.getSnappedOnEdge(nodes, endPoint, technicalAllowed = technicalAllowed)

            return getShortestPathJob(
                startPoint = snapStart,
                endPoint = snapEnd,
                technicalAllowed = technicalAllowed,
            ).map { PointD(it.x, it.y) }
        }
    }

    private suspend fun waitForNodes(): MutableSet<Node> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }

    private fun MutableSet<Node>.createAndConnect(
        snap: SnappedOnEdge,
    ): Node {
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