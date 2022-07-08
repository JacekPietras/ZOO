package com.jacekpietras.zoo.map.extensions

import com.jacekpietras.mapview.model.ComposablePaint
import com.jacekpietras.mapview.ui.MapViewLogic
import com.jacekpietras.zoo.map.model.MapVolatileViewState
import com.jacekpietras.zoo.map.model.MapWorldViewState
import timber.log.Timber

internal fun MapViewLogic<ComposablePaint>.applyToMap(viewState: MapWorldViewState) {
    Timber.e("dupa apply mapWorld")
    worldData = MapViewLogic.WorldData(
        bounds = viewState.worldBounds,
        objectList = viewState.mapData,
    )
}

internal fun MapViewLogic<ComposablePaint>.applyToMap(viewState: MapVolatileViewState) {
    Timber.e("dupa apply volatile")
    userData = MapViewLogic.UserData(
        userPosition = viewState.userPosition,
        compass = viewState.compass,
        objectList = viewState.mapData,
    )
}
