package com.jacekpietras.zoo.tracking.contract.interactor

interface OnLightSensorUpdateUseCase {

    operator fun invoke(luminance: Float)
}