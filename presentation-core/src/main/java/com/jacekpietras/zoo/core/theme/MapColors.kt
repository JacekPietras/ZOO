@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class MapColors(
    private val nightTheme: Boolean = false,
) {

    val colorPrimary = Palette.primary onNight Palette.primaryDark

    val colorMapBackground = Color(0xFFf8f9fa) onNight Color(0xFF121a26)
    val colorMapBuilding = Color(0xFFf1f3f4) onNight Color(0xFF2f3a47)
    val colorMapGrass = Color(0xFFD2E7D3) onNight Color(0xFF2F6832)
    val colorMapForest = Color(0xFFAAD5AC) onNight Color(0xFF2D4E2E)
    val colorMapWater = Color(0xFF2487B9) onNight Color(0xFF215068)
    val colorMapBuildingBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)
    val colorMapRoad = Palette.white onNight Color(0xFF4E5C6C)
    val colorMapRoadVisited = Color(0xFFD1FAE1) onNight Color(0xFF447C43)
    val colorMapRoadBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)
    val colorMapTaken = Palette.primary onNight Palette.primaryDark
    val colorMapTechnical = Color(0xFFFFF0F0) onNight Color(0xFF221717)
    val colorMapTechnicalBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)

    val colorSmallMapBackground = colorMapForest // Color(0xFFA1DAB8) onNight Color(0xFFA1DAB8)
    val colorSmallMapBuilding = Color(0xFF768A7E) onNight Color(0xFF768A7E)
    val colorSmallMapRoad = Palette.white onNight Palette.white
    val colorSmallMapAnimal = Color(0xFFF44336) onNight Color(0xFFF44336)

    private infix fun <T> T.onNight(right: T): T = if (nightTheme) right else this
}
