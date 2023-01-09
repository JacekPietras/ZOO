package com.jacekpietras.zoo.domain.feature.pathfinder

import com.jacekpietras.geometry.PointD
import com.jacekpietras.zoo.domain.feature.map.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.feature.pathfinder.model.Node
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn
import com.jacekpietras.zoo.domain.feature.pathfinder.model.SnappedOn.SnappedOnEdge
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class GraphAnalyzer {

    private var nodes: MutableSet<Node>? = null
    private val snapper = PointSnapper()
    private val mutex = Mutex()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes = NodeSetFactory(roads, technical).create()
    }

    fun isInitialized(): Boolean = nodes != null

    suspend fun getTerminalPoints(): List<PointD> =
        mutex.withLock {
            waitForNodes()
                .asSequence()
                .filter { it.edges.size > 2 }
                .map(Node::point)
                .toList()
        }

    suspend fun getSnappedPointOnEdge(
        point: PointD,
        technicalAllowed: Boolean,
    ): SnappedOnEdge =
        mutex.withLock {
            snapper.getSnappedOnEdge(
                waitForNodes(),
                point,
                technicalAllowed
            )
        }

    suspend fun getShortestPathWithContext(
        endPoint: SnappedOnEdge,
        startPoint: SnappedOnEdge,
        technicalAllowed: Boolean = false,
    ): List<Node> =
        mutex.withLock {
            getShortestPathJob(
                start = startPoint.makeNode(),
                end = endPoint.makeNode(),
                technicalAllowed = technicalAllowed,
            )
        }

    internal suspend fun getShortestPath(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowedAtStart: Boolean = true,
        technicalAllowedAtEnd: Boolean = false,
    ): List<PointD> {
        mutex.withLock {
            val nodes = waitForNodes()

            if (startPoint == null) return listOf(endPoint)
            if (startPoint == endPoint) return listOf(endPoint)
            if (nodes.isEmpty()) return listOf(endPoint)

            val snapStart = snapper.getSnappedOnEdge(nodes, startPoint, technicalAllowed = technicalAllowedAtStart).makeNode()
            val snapEnd = snapper.getSnappedOnEdge(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd).makeNode()

            return getShortestPathJob(
                start = snapStart,
                end = snapEnd,
                technicalAllowed = technicalAllowedAtEnd,
            ).map { PointD(it.x, it.y) }
        }
    }

    internal suspend fun getShortestPathParallel(
        endPoint: PointD,
        startPoint: PointD?,
        technicalAllowedAtStart: Boolean = true,
        technicalAllowedAtEnd: Boolean = false,
    ): List<PointD> {
        mutex.withLock {
            val nodes = waitForNodes()

            if (startPoint == null) return listOf(endPoint)
            if (nodes.isEmpty()) return listOf(endPoint)

            val snapStart = snapper.getSnappedOn(nodes, startPoint, technicalAllowed = technicalAllowedAtStart)
            val snapEnd = snapper.getSnappedOn(nodes, endPoint, technicalAllowed = technicalAllowedAtEnd)

            if (snapStart == snapEnd) return listOf(snapEnd.point)

            return getShortestPathJobParallel(
                start = snapStart,
                end = snapEnd,
                technicalAllowed = technicalAllowedAtEnd,
            ).map(Node::point)
        }
    }

    private suspend fun getShortestPathJob(
        start: SnappedNode,
        end: SnappedNode,
        technicalAllowed: Boolean = false,
    ): List<Node> =
        Dijkstra(
            vertices = waitForNodes(),
            start = start.node,
            end = end.node,
            technicalAllowed = technicalAllowed
        )
            .getPath()
            .also { revertConnections(start, end) }

    private suspend fun getShortestPathJobParallel(
        start: SnappedOn,
        end: SnappedOn,
        technicalAllowed: Boolean = false,
    ): List<Node> =
        ParallelDijkstra(
            vertices = waitForNodes(),
            technicalAllowed = technicalAllowed,
        ).calculate(
            start = start,
            end = end,
        )

    private suspend fun revertConnections(vararg nodes: SnappedNode) {
        nodes
            .filterIsInstance<NewSnappedNode>()
            .map(NewSnappedNode::node)
            .run { revertConnections(this) }
    }

    private suspend fun revertConnections(cleanupList: List<Node>) {
        cleanupList
            .reversed()
            .forEach { fake ->
                val edges = fake.edges.toList()
                if (edges.size > 2) throw IllegalStateException("more than two edges in fake node")

                val edge1 = edges[0]
                val edge2 = edges[1]
                val technical = edge1.technical

                edge1.node.disconnect(fake)
                edge2.node.disconnect(fake)

                edge1.node.connectAndCalc(edge2.node, technical, backward = edge1.backward)
                edge2.node.connectAndCalc(edge1.node, technical, backward = edge2.backward)

                val removed = waitForNodes().remove(fake)
                if (!removed) {
                    waitForNodes().removeAll { it.point == fake.point }
                }
            }
    }

    internal suspend fun waitForNodes(): MutableSet<Node> {
        while (nodes == null) {
            delay(100)
        }
        return checkNotNull(nodes)
    }

    private suspend fun SnappedOnEdge.makeNode(): SnappedNode =
        when (point) {
            near1.point -> SnappedNode(near1)
            near2.point -> SnappedNode(near2)
            else -> createAndConnect(this)
        }

    private suspend fun createAndConnect(
        snap: SnappedOnEdge,
    ): NewSnappedNode {
        val node = Node(snap.point)

        val edge1 = snap.near1.edges.first { it.node == snap.near2 }
        snap.near1.connectAndCalc(node, edge1.technical, backward = edge1.backward)
        node.connectAndCalc(snap.near2, edge1.technical, backward = edge1.backward)

        val edge2 = snap.near2.edges.first { it.node == snap.near1 }
        snap.near2.connectAndCalc(node, edge2.technical, backward = edge2.backward)
        node.connectAndCalc(snap.near1, edge2.technical, backward = edge2.backward)

        snap.near1.disconnect(snap.near2)
        snap.near2.disconnect(snap.near1)
        waitForNodes().add(node)

        return NewSnappedNode(node)
    }

    private open class SnappedNode(
        val node: Node,
    )

    private class NewSnappedNode(
        node: Node,
    ) : SnappedNode(node)
}