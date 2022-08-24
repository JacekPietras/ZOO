package com.jacekpietras.zoo.tracking.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Process
import android.os.Process.killProcess
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.LifecycleService
import com.jacekpietras.zoo.tracking.R
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveCompassEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveLightSensorEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.ObserveNavigationEnabledUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnCompassUpdateUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnLightSensorUpdateUseCase
import com.jacekpietras.zoo.tracking.contract.interactor.OnLocationUpdateUseCase
import com.jacekpietras.zoo.tracking.listener.CompassListenerCompat
import com.jacekpietras.zoo.tracking.listener.GpsLocationListenerCompat
import com.jacekpietras.zoo.tracking.listener.GpsStatusListenerCompat
import com.jacekpietras.zoo.tracking.listener.LightSensorListenerCompat
import com.jacekpietras.zoo.tracking.utils.observe
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressLint("Registered")
class TrackingService : LifecycleService() {

    private val onLocationUpdateUseCase: OnLocationUpdateUseCase by inject()
    private val onCompassUpdateUseCase: OnCompassUpdateUseCase by inject()
    private val onLightSensorUpdateUseCase: OnLightSensorUpdateUseCase by inject()
    private val observeNavigationEnabledUseCase: ObserveNavigationEnabledUseCase by inject()
    private val observeCompassEnabledUseCase: ObserveCompassEnabledUseCase by inject()
    private val observeLightSensorEnabledUseCase: ObserveLightSensorEnabledUseCase by inject()
    private var serviceUtils: ServiceUtils? = null
    private val gpsLocationListener = GpsLocationListenerCompat(
        onLocationChanged = { time, lat, lon, accuracy ->
            onLocationUpdateUseCase(time, lat, lon, accuracy)
        },
        onGpsStatusChanged = { enabled ->
            if (enabled) Timber.i("Gps Status Enabled (a)")
            else Timber.i("Gps Status Disabled (a)")
        },
    )
    private val gpsStatusListener = GpsStatusListenerCompat { enabled ->
        if (enabled) Timber.i("Gps Status Enabled (b)")
        else Timber.i("Gps Status Disabled (b)")
    }
    private val compassListener = CompassListenerCompat { angle ->
        Timber.i("Compass angle $angle")
        onCompassUpdateUseCase(angle)
    }
    private var compassIsWorking = false
    private val lightSensorListener = LightSensorListenerCompat { luminance ->
        onLightSensorUpdateUseCase(luminance)
    }
    private var lightSensorIsWorking = false

    override fun onCreate() {
        super.onCreate()
        serviceUtils = ServiceUtils(this, 222)
        serviceUtils?.setForegroundNotification(
            context = this,
            channelId = NORMAL_NOTIFICATION_CHANNEL,
            titleRes = R.string.location_service_notification_title,
            descriptionRes = R.string.location_service_notification_description,
            smallIconRes = R.drawable.tracking_ic_my_location_24,
            actionRes = R.string.location_service_stop_text,
            actionBroadcast = ACTION_STOP_SERVICE,
        )

        observeNavigationEnabledUseCase.run().observe(lifecycleOwner = this) { enabled ->
            if (!enabled) {
                stopEverything()
                stopSelf()
            }
        }

        observeCompassEnabledUseCase.run().observe(lifecycleOwner = this) { enabled ->
            if (enabled) {
                compassStart()
            } else {
                compassStop()
            }
        }

        observeLightSensorEnabledUseCase.run().observe(lifecycleOwner = this) { enabled ->
            if (enabled) {
                lightStart()
            } else {
                lightStop()
            }
        }
    }

    private fun stopEverything() {
        compassStop()
        lightStop()
        navigationStop()
    }

    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (ACTION_STOP_SERVICE == intent?.action) {
            compassStop()
            lightStop()
            navigationStop()
            stopSelf()
        } else {
            navigationStart()
        }

        return START_STICKY
    }

    private fun navigationStart() {
        if (gpsLocationListener.noPermissions(this)) return

        gpsLocationListener.addLocationListener(this)
        gpsStatusListener.addStatusListener(this)
    }

    private fun navigationStop() {
        gpsLocationListener.removeLocationListener()
        gpsStatusListener.removeStatusListener()

        serviceUtils?.removeNotification()
        stopForeground(true)
        if (!isActivityRunning(this)) {
            killProcess(Process.myPid())
        }
    }

    private fun compassStart() {
        if (!compassIsWorking) {
            compassListener.addCompassListener(this)
            compassIsWorking = true
        }
    }

    private fun compassStop() {
        if (compassIsWorking) {
            Timber.v("Compass stopped")
            compassListener.removeCompassListener()
            compassIsWorking = false
        }
    }

    private fun lightStart() {
        if (!lightSensorIsWorking) {
            lightSensorListener.addLightSensorListener(this)
            lightSensorIsWorking = true
        }
    }

    private fun lightStop() {
        if (lightSensorIsWorking) {
            Timber.v("Light sensor stopped")
            lightSensorListener.removeLightSensorListener()
            lightSensorIsWorking = false
        }
    }

    companion object {

        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private val NORMAL_NOTIFICATION_CHANNEL = "Channel" + R.id.normal_notification_channel

        fun start(context: Context) {
            if (!isServiceRunning(context, TrackingService::class.java)) {
                initTrackingNotificationChannel(context, NORMAL_NOTIFICATION_CHANNEL)
                startForegroundService(context, Intent(context, TrackingService::class.java))
            }
        }
    }
}
