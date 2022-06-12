package com.jacekpietras.zoo.tracking.interactor

interface OnLightSensorUpdate {

    operator fun invoke(light: Boolean, value:Float)
}