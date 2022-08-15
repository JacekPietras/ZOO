@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class MapColors(
    private val nightTheme: Boolean = false,
) {

    val colorPrimary = Palette.primary onNight Palette.primaryDark
    val colorAccent = Color(0xFFEF6C00) onNight Color(0xFFD84315)

    val colorMapGrass = Color(0xFFD2E7D3) onNight Color(0xFF2F6832)
    val colorMapForest = Color(0xFF8EBB90) onNight Color(0xFF2D4E2E)
    val colorMapWater = Color(0xFF2487B9) onNight Color(0xFF215068)

    val colorMapBuilding = Color(0xFFf1f3f4) onNight Color(0xFF2f3a47)
    val colorMapBuildingBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)

    val colorMapRoad = Palette.white onNight Color(0xFF4E5C6C)
    val colorMapRoadBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)
    val colorMapRoadVisited = Color(0xFFD1FAE1) onNight Color(0xFF447C43)
    val colorMapTaken = colorPrimary
    val colorMapNavigation = colorAccent
    val colorMapLines = Color(0xFFC9765D) onNight Color(0xFFD84315)
    val colorMapTechnical = Color(0xFFFFF0F0) onNight Color(0xFF221717)

    val colorSmallMapBackground = colorMapForest
    val colorSmallMapBuilding = Color(0xFF768A7E) onNight Color(0xFF768A7E)
    val colorSmallMapRoad = Palette.white
    val colorSmallMapAnimal = Color(0xFFF44336) onNight Color(0xFFF44336)

    private infix fun <T> T.onNight(right: T): T = if (nightTheme) right else this
}
