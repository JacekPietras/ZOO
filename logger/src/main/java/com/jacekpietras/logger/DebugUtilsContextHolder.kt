package com.jacekpietras.logger

import android.content.Context

object DebugUtilsContextHolder {

    private var contextInternal: Context? = null
    internal val context: Context
        get() {
            return contextInternal
                ?: throw IllegalStateException("Logging module not initialized properly")
        }

    fun init(context: Context) {
        this.contextInternal = context
    }

    fun destroy() {
        contextInternal = null
    }
}