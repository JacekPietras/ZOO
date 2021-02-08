package com.jacekpietras.zoo.tracking

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import androidx.core.content.ContextCompat.startForegroundService
import com.jacekpietras.zoo.domain.interactor.InsertUserPositionUseCase
import com.jacekpietras.zoo.domain.model.GpsHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressLint("Registered")
class TrackingService : Service() {

    //TODO try listener from google play

    val insertUserPositionUseCase: InsertUserPositionUseCase by inject()

    private var serviceUtils: ServiceUtils? = null
    private var locationManager: LocationManager? = null
    private val gpsLocationListener = GpsLocationListenerCompat(
        onLocationChanged = { time, lat, lon ->
            CoroutineScope(Dispatchers.IO).launch {
                insertUserPositionUseCase(GpsHistoryEntity(time, lat, lon))
            }
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

        locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager
        gpsLocationListener.addLocationListener(this)
        gpsStatusListener.addStatusListener(this)
    }

    private fun navigationStop() {
        serviceUtils?.removeNotification()
        stopForeground(true)
        gpsLocationListener.removeLocationListener()
        gpsStatusListener.removeStatusListener()
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
