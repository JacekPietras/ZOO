package com.jacekpietras.zoo.tracking.interactor

interface OnCompassUpdate {

    operator fun invoke(angle: Float)
}