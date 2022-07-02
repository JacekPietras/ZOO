package com.jacekpietras.zoo.map.service

import android.content.Context
import com.jacekpietras.zoo.tracking.service.TrackingService

internal class TrackingServiceStarter(private val context: Context) {

  fun  run(){
        TrackingService.start(context)

    }
}