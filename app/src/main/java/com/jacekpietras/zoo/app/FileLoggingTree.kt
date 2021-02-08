package com.jacekpietras.zoo.app

import android.util.Log
import com.jacekpietras.logger.LogSupport
import timber.log.Timber

class FileLoggingTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.ERROR -> LogSupport.writeDown(FileLogChannel.TIMBER_ERROR, "$tag: $message", t)
            Log.WARN -> LogSupport.writeDown(FileLogChannel.TIMBER_WARN, "$tag: $message", t)
        }
    }
}