package com.jacekpietras.zoo.tracking.interactor

interface OnLocationUpdate {

    operator fun invoke(time: Long, lat: Double, lon: Double)
}