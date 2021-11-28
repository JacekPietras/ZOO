package com.jacekpietras.zoo.domain.interactor

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity.PathEntity
import com.jacekpietras.zoo.domain.model.VisitedRoadEdge
import com.jacekpietras.zoo.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveVisitedRoadsUseCase(
    private val mapRepository: MapRepository,
) {

    fun run(): Flow<List<PathEntity>> =
        mapRepository.observeVisitedRoads()
            .map { edges ->

                val paths = mutableListOf<PathEntity>().apply {
                    edges.forEach { edge ->
                        when (edge) {
                            is VisitedRoadEdge.Fully -> add(edge.toPath())
                            is VisitedRoadEdge.Partially -> addAll(edge.toPath())
                        }
                    }
                }

                PathMerger(paths).run()
            }
}

internal class PathMerger(private val paths: List<PathEntity>) {
    private val connected = mutableListOf<PathEntity>()
    private val result = mutableListOf<PathEntity>()

    fun run(): List<PathEntity> {

        paths.forEach { next ->
            if (next !in connected) {
                connected.add(next)
                val merged = expandToRight(expandToLeft(next))
                result.add(merged)
            }
        }
        return result
    }

    private fun expandToLeft(next: PathEntity): PathEntity {
        val left = findNeighbour(next, paths, next.first)
        return if (left != null) {
            connected.add(left)

            if (next.first == left.first) {
                expandToLeft(left.last + next)
            } else {
                expandToLeft(left.first + next)
            }
        } else {
            next
        }
    }

    private fun expandToRight(next: PathEntity): PathEntity {
        val right = findNeighbour(next, paths, next.last)
        return if (right != null) {
            connected.add(right)

            if (next.last == right.last) {
                expandToLeft(next + right.first)
            } else {
                expandToLeft(next + right.last)
            }
        } else {
            next
        }
    }

    private fun findNeighbour(first: PathEntity, list: List<PathEntity>, point: PointD): PathEntity? {
        list.forEach {
            if ((it.first == point || it.last == point) && first !== it && it !in connected) {
                return it
            }
        }
        return null
    }

    private val PathEntity.first get() = vertices.first()
    private val PathEntity.last get() = vertices.last()

    private operator fun PointD.plus(right: PathEntity): PathEntity =
        PathEntity(
            ArrayList<PointD>(right.vertices.size + 1).apply {
                add(this@plus)
                addAll(right.vertices)
            }
        )

    private operator fun PathEntity.plus(right: PointD): PathEntity =
        PathEntity(this.vertices + right)
}