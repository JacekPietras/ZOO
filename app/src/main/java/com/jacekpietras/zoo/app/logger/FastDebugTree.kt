package com.jacekpietras.zoo.app.logger

import android.annotation.SuppressLint
import android.util.Log
import timber.log.Timber

class FastDebugTree : Timber.Tree() {

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when (priority) {
            Log.ERROR -> Log.e("E:", message)
            Log.WARN ->  Log.w("E:", message)
            Log.INFO ->  Log.i("I:", message)
            Log.DEBUG ->  Log.d("D:", message)
        }
    }
}