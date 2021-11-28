package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.zoo.domain.model.MapItemEntity

internal class PathMerger(private val paths: List<MapItemEntity.PathEntity>) {
    private val connected = mutableListOf<MapItemEntity.PathEntity>()
    private val result = mutableListOf<MapItemEntity.PathEntity>()

    fun run(): List<MapItemEntity.PathEntity> {

        paths.forEach { next ->
            if (next !in connected) {
                connected.add(next)
                val merged = expandToRight(expandToLeft(next))
                result.add(merged)
            }
        }
        return result
    }

    private fun expandToLeft(next: MapItemEntity.PathEntity): MapItemEntity.PathEntity {
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

    private fun expandToRight(next: MapItemEntity.PathEntity): MapItemEntity.PathEntity {
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

    private fun findNeighbour(first: MapItemEntity.PathEntity, list: List<MapItemEntity.PathEntity>, point: PointD): MapItemEntity.PathEntity? {
        list.forEach {
            if ((it.first == point || it.last == point) && first !== it && it !in connected) {
                return it
            }
        }
        return null
    }

    private val MapItemEntity.PathEntity.first get() = vertices.first()
    private val MapItemEntity.PathEntity.last get() = vertices.last()

    private operator fun PointD.plus(right: MapItemEntity.PathEntity): MapItemEntity.PathEntity =
        MapItemEntity.PathEntity(
            ArrayList<PointD>(right.vertices.size + 1).apply {
                add(this@plus)
                addAll(right.vertices)
            }
        )

    private operator fun MapItemEntity.PathEntity.plus(right: PointD): MapItemEntity.PathEntity =
        MapItemEntity.PathEntity(this.vertices + right)
}
