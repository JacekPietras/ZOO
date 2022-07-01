package com.jacekpietras.zoo.tracking.contract.interactor

interface OnLocationUpdateUseCase {

    operator fun invoke(time: Long, lat: Double, lon: Double)
}