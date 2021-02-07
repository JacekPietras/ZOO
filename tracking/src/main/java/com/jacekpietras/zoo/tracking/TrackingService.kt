package com.jacekpietras.zoo.tracking

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.*
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.IBinder
import androidx.core.content.ContextCompat
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

    private var serviceUtils: ServiceUtils? = null
    private var locationManager: LocationManager? = null
    private val locationListener = LocationListener {
        CoroutineScope(Dispatchers.IO).launch {
            insertUserPositionUseCase(GpsHistoryEntity(it.time, it.latitude, it.longitude))
        }
    }

    @Suppress("DEPRECATION")
    private val statusListener: Any = if (SDK_INT >= VERSION_CODES.N) {
        object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                Timber.i("Gps Status $status")
            }
        }
    } else {
        GpsStatus.Listener {
            when (it) {
                GpsStatus.GPS_EVENT_STARTED -> Timber.i("Gps Status STARTED")
                GpsStatus.GPS_EVENT_STOPPED -> Timber.i("Gps Status STOPPED")
            }
        }
    }
    val insertUserPositionUseCase: InsertUserPositionUseCase by inject()

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

        val success = navigationStart()
        if(!success) stopSelf()
    }

    override fun onDestroy() {
        navigationStop()
        stopForeground(true)
        serviceUtils?.removeNotification()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent.action) {
            navigationStop()
            stopForeground(true)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun navigationStart(): Boolean {
        if (noPermissions()) return false

        locationManager = getSystemService(LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) return false
        locationManager?.removeUpdates(locationListener)

        val providerFound = locationManager?.requestLocationUpdates() ?: false
        if (!providerFound) return false

        try {
            @Suppress("DEPRECATION")
            if (statusListener is GpsStatus.Listener) {
                locationManager?.addGpsStatusListener(statusListener)
            } else if (SDK_INT >= VERSION_CODES.N && statusListener is GnssStatus.Callback) {
                locationManager?.registerGnssStatusCallback(statusListener)
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Location permissions not granted")
            return false
        }

        return true
    }

    private fun navigationStop() {
        locationManager?.removeUpdates(locationListener)
        @Suppress("DEPRECATION")
        if (statusListener is GpsStatus.Listener) {
            locationManager?.removeGpsStatusListener(statusListener)
        } else if (SDK_INT >= VERSION_CODES.N && statusListener is GnssStatus.Callback) {
            locationManager?.unregisterGnssStatusCallback(statusListener)
        }
    }

    private fun LocationManager.requestLocationUpdates(): Boolean =
        requestLocationUpdates(GPS_PROVIDER)
                || requestLocationUpdates(NETWORK_PROVIDER)
                || requestLocationUpdates(PASSIVE_PROVIDER)

    @SuppressLint("MissingPermission")
    private fun LocationManager.requestLocationUpdates(provider: String): Boolean {
        when {
            noPermissions() -> Timber.e("Permissions not granted")
            allProviders.contains(provider) -> {
                try {
                    requestLocationUpdates(GPS_PROVIDER, 5000, 0f, locationListener)
                    return true
                } catch (e: NullPointerException) {
                    Timber.w(e, "Location cannot be accessed")
                } catch (e: IllegalArgumentException) {
                    Timber.w(e, "Location cannot be accessed")
                }
            }
        }
        return false
    }

    private fun noPermissions(): Boolean =
        !granted(permission.ACCESS_FINE_LOCATION) && !granted(permission.ACCESS_COARSE_LOCATION)

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED

    private fun isGpsEnabled(): Boolean {
        return locationManager?.isProviderEnabled(GPS_PROVIDER) ?: false
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
