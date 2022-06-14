package com.jacekpietras.zoo.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Process
import android.os.Process.killProcess
import androidx.core.content.ContextCompat.startForegroundService
import androidx.lifecycle.LifecycleService
import com.jacekpietras.zoo.tracking.interactor.*
import com.jacekpietras.zoo.tracking.utils.*
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressLint("Registered")
class TrackingService : LifecycleService() {

    private val onLocationUpdate: OnLocationUpdate by inject()
    private val onCompassUpdate: OnCompassUpdate by inject()
    private val onLightSensorUpdate: OnLightSensorUpdate by inject()
    private val observeCompassEnabledUseCase: ObserveCompassEnabledUseCase by inject()
    private val observeLightSensorEnabledUseCase: ObserveLightSensorEnabledUseCase by inject()
    private var serviceUtils: ServiceUtils? = null
    private val gpsLocationListener = GpsLocationListenerCompat(
        onLocationChanged = { time, lat, lon ->
            onLocationUpdate(time, lat, lon)
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
        onCompassUpdate(angle)
    }
    private var compassIsWorking = false
    private val lightSensorListener = LightSensorListenerCompat { luminance ->
        onLightSensorUpdate(luminance)
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
            smallIconRes = R.drawable.ic_my_location_24,
            actionRes = R.string.location_service_stop_text,
            actionBroadcast = ACTION_STOP_SERVICE,
        )

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

    override fun onDestroy() {
        compassStop()
        lightStop()
        navigationStop()
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
