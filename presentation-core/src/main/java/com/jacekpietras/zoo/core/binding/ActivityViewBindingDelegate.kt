package com.jacekpietras.zoo.core.binding

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding

class ActivityViewBindingDelegate<T : ViewBinding>(
    private val activity: AppCompatActivity,
    private val viewBindingFactory: (LayoutInflater) -> T
) : Lazy<T>, LifecycleEventObserver {

    private var binding: T? = null

    override val value: T
        get() {
            if (binding == null) {
                bindView()
            }
            return checkNotNull(binding)
        }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun isInitialized(): Boolean =
        binding != null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE && binding == null) {
            bindView()
        }
    }

    private fun bindView() {
        binding = viewBindingFactory(activity.layoutInflater)
        activity.setContentView(checkNotNull(binding).root)
    }
}

fun <T : ViewBinding> AppCompatActivity.viewBinding(viewBindingFactory: (LayoutInflater) -> T) =
    ActivityViewBindingDelegate(this, viewBindingFactory)