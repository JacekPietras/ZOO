package com.jacekpietras.zoo.map.extensions

import com.jacekpietras.mapview.logic.MapViewLogic
import com.jacekpietras.mapview.logic.UserData
import com.jacekpietras.mapview.logic.WorldData
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState

internal fun <T> MapViewLogic<T>.applyToMap(viewState: MapWorldViewState) {
    worldData = WorldData(
        bounds = viewState.worldBounds,
        objectList = viewState.mapData,
    )
}

internal fun <T> MapViewLogic<T>.applyToMap(viewState: MapVolatileViewState) {
    userData = UserData(
        userPosition = viewState.userPosition,
        compass = viewState.compass,
        objectList = viewState.mapData,
    )
}
