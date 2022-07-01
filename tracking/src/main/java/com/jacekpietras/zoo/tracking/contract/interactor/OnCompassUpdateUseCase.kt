package com.jacekpietras.zoo.tracking.contract.interactor

interface OnCompassUpdateUseCase {

    operator fun invoke(angle: Float)
}