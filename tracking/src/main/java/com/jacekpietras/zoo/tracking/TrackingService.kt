package com.jacekpietras.zoo.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.os.Process.killProcess
import androidx.core.content.ContextCompat.startForegroundService
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressLint("Registered")
class TrackingService : Service() {

    private val onLocationUpdate: OnLocationUpdate by inject()
    private val onCompassUpdate: OnCompassUpdate by inject()
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
    }

    override fun onDestroy() {
        navigationStop()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent?.action) {
            navigationStop()
            stopSelf()
        } else {
            navigationStart()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun navigationStart() {
        if (gpsLocationListener.noPermissions(this)) return

        gpsLocationListener.addLocationListener(this)
        gpsStatusListener.addStatusListener(this)
        compassListener.addCompassListener(this)
    }

    private fun navigationStop() {
        gpsLocationListener.removeLocationListener()
        gpsStatusListener.removeStatusListener()
        compassListener.removeCompassListener()

        serviceUtils?.removeNotification()
        stopForeground(true)
        if (!isActivityRunning(this)) {
            killProcess(Process.myPid())
        }
    }

    companion object {
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private val NORMAL_NOTIFICATION_CHANNEL = "Channel" + R.id.normal_notification_channel

        fun start(context: Context) {
            initTrackingNotificationChannel(context, NORMAL_NOTIFICATION_CHANNEL)
            startForegroundService(context, Intent(context, TrackingService::class.java))
        }
    }
}
