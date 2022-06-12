package com.jacekpietras.zoo.app

import com.jacekpietras.zoo.tracking.interactor.OnLightSensorUpdate

class OnLightSensorUpdateImpl : OnLightSensorUpdate {

    override fun invoke(light: Boolean, value: Float) {
        // todo use light sensor
    }
}