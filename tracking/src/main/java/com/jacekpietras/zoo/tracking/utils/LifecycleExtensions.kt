package com.jacekpietras.zoo.tracking.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


internal fun LifecycleOwner.observeReturn(onReturn: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        private var stopped = false

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            stopped = true
        }

        override fun onResume(owner: LifecycleOwner) {
            if (stopped) {
                onReturn()
                lifecycle.removeObserver(this)
            }
        }
    })
}