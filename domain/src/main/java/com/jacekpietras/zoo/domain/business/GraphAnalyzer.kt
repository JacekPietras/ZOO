package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.business.Snapper.Snapped
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import timber.log.Timber
import kotlin.system.measureTimeMillis

object GraphAnalyzer {

    private var nodes = mutableSetOf<Node>()
    private val snapper = Snapper()

    fun initialize(roads: List<PathEntity>, technical: List<PathEntity>) {
        nodes = Builder(roads, technical).build()
    }

    fun getTerminalPoints(): List<PointD> =
        nodes
            .asSequence()
            .filter { it.edges.size > 2 }
            .map { it.point }
            .toList()

    fun getSnapped(point: PointD): PointD =
        snapper.getSnappedPoint(nodes, point)

    fun getShortestPath(endPoint: PointD, startPoint: PointD?): List<PointD> {
        if (startPoint == null) return listOf(endPoint)

        val snapA = snapper.getSnappedEdge(nodes, endPoint)
        val snapB = snapper.getSnappedEdge(nodes, startPoint)

        val cleanupList: MutableList<Node> = mutableListOf()

        val start = when (snapB.point) {
            snapB.near1.point -> snapB.near1
            snapB.near2.point -> snapB.near2
            else -> {
                 createAndConnect(snapB)
                    .also { cleanupList.add(it) }
            }
        }

        val end = when (snapA.point) {
            snapA.near1.point -> snapA.near1
            snapA.near2.point -> snapA.near2
            else -> {
                createAndConnect(snapA)
                    .also { cleanupList.add(it) }
            }
        }

        var result: List<PointD>? = null

        //todo it's stress test
        var time1 = measureTimeMillis {
            for (i in 0..50) {
                result = Dijkstra(nodes, start, end).getPath()
                    .map { PointD(it.x, it.y) }
            }
        }

        val time2 = measureTimeMillis {
            for (i in 0..100) {
                result = DijkstraTest(nodes, start, end).getPath()
                    .map { PointD(it.x, it.y) }
            }
        }

        time1 += measureTimeMillis {
            for (i in 0..50) {
                result = Dijkstra(nodes, start, end).getPath()
                    .map { PointD(it.x, it.y) }
            }
        }

        Timber.e("Dijkstra: $time1, test: $time2, diff ${time1 - time2}")

        cleanupList.forEach { fake ->
            val edges = fake.edges.toList()
            if (edges.size > 2) throw IllegalStateException("more than two edges in fake node")

            val edge1 = edges[0]
            val edge2 = edges[1]

            edge1.node.disconnect(fake)
            edge2.node.disconnect(fake)
            nodes.remove(fake)
        }

        return result!!
    }

    private fun createAndConnect(snap: Snapped):Node{
        val node = Node(snap.point)
        val edge = snap.near1.edges.first { it.node == snap.near2 }

        snap.near1.connectAndCalc(node, edge.technical, backward = false)
        snap.near2.connectAndCalc(node, edge.technical, backward = true)
        node.connectAndCalc(snap.near1, edge.technical, backward = false)
        node.connectAndCalc(snap.near2, edge.technical, backward = true)
        nodes.add(node)

        return node
    }
}