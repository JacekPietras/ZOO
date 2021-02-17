package com.jacekpietras.zoo.tracking

interface OnCompassUpdate {

    operator fun invoke(angle: Float)
}