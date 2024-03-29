package com.jacekpietras.zoo.map.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

internal fun Context.getActivity(): Activity {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    throw IllegalStateException("Activity not available")
}
