package com.jacekpietras.logger

import android.content.Context
import java.lang.IllegalStateException

object DebugUtilsContextHolder {
    // We use contextInternal because it can be null so
    // when we are finishing using it we can null it
    // Additional advantage is that
    // context is not nullable
    // so we don't need to think about null when using it

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