@file:Suppress("MemberVisibilityCanBePrivate", "unused", "CanBeParameter")

package com.jacekpietras.zoo.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.jacekpietras.zoo.core.theme.Palette.secondarySurface

@Immutable
class MapColors(
    val nightTheme: Boolean = false,
) {

    val colorPrimary = Palette.primary onNight Palette.primaryDark
    val colorAccent = Color(0xFFEF6C00) onNight Color(0xFFD84315)
    val userPositionBorder = Palette.white
    val userPositionShadow = Palette.black.copy(alpha = 0.07f) onNight Palette.black.copy(alpha = 0.2f)

    val colorMapGrass = secondarySurface onNight Color(0xFF152016)
    val colorMapForest = Color(0xFF9CC49E) onNight Color(0xFF1A2E1A)
    val colorMapWater = Color(0xFF84cdc9) onNight Color(0xFF114946)

    val colorMapBuilding = Color(0xFFCAD4DA) onNight Color(0xFF2f3a47)
    val colorMapAviary = Color(0xFFDDE3E7) onNight Color(0xFF415164)
    val colorMapBuildingBorder = Color(0xFFA9B3B9) onNight Color(0xFF0E141E)

    val colorMapRoad = Palette.white onNight Color(0xFF667586)
    val colorMapRoadBorder = Color(0xFFD7DBDD) onNight Color(0xFF0E141E)
    val colorMapRoadVisited = Color(0xFFD1FAE1) onNight Color(0xFF447C43)
    val colorMapTaken = colorPrimary
    val colorMapNavigation = colorAccent//.copy(alpha = 0.6f)
    val colorMapNavigationArrow = Palette.white
    val colorMapLines = Color(0xFFAA834F) onNight Color(0xFF584020)
    val colorMapTechnical = Color(0xFFFFF0F0) onNight Color(0xFF866666)

    val colorSmallMapBackground = colorMapForest
    val colorSmallMapBuilding = colorMapBuilding onNight colorMapAviary // Color(0xFF768A7E) onNight Color(0xFF768A7E)
    val colorSmallMapRoad = colorMapRoad // Palette.white
    val colorSmallMapAnimal = colorMapNavigation // Color(0xFFF44336) onNight Color(0xFFF44336)

    private infix fun <T> T.onNight(right: T): T = if (nightTheme) right else this
}
