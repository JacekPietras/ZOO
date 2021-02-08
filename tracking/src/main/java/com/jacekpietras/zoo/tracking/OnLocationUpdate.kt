package com.jacekpietras.zoo.tracking

interface OnLocationUpdate {

    operator fun invoke(time: Long, lat: Double, lon: Double)
}