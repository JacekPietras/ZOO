package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity

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
//            .filter { it.edges.size <= 1 }
            .map { it.point }
            .toList()

    fun getSnapped(point: PointD): PointD =
        snapper.getSnappedPoint(nodes, point)

    fun getShortestPath(a: PointD, b: PointD?): List<PointD> {
        if (b == null) return listOf(a)

        //todo build graph with jumps and use both point of edges
        val snapA = snapper.getSnappedEdge(nodes, a)
        val snapB = snapper.getSnappedEdge(nodes, b)

        return Dijkstra(nodes, snapA.second, snapB.second).getPath()
            .map { PointD(it.x, it.y) }
    }
}