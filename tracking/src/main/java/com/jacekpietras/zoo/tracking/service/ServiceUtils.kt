package com.jacekpietras.zoo.tracking.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.jacekpietras.zoo.tracking.R
import com.jacekpietras.zoo.tracking.service.TrackingService.Companion.ACTION_STOP_SERVICE
import timber.log.Timber

internal class ServiceUtils(
    private val service: Service,
    private val notificationId: Int,
) {

    private var isNotification = false

    fun setForegroundNotification(
        context: Context,
        channelId: String,
        @StringRes titleRes: Int = 0,
        title: String? = if (titleRes != 0) context.getString(titleRes) else null,
        @StringRes descriptionRes: Int = 0,
        description: String? = if (descriptionRes != 0) context.getString(descriptionRes) else null,
        @StringRes infoRes: Int = 0,
        info: String? = if (infoRes != 0) context.getString(infoRes) else null,
        @StringRes actionRes: Int = 0,
        actionText: String? = if (actionRes != 0) context.getString(actionRes) else null,
        actionBroadcast: String? = null,
        @DrawableRes smallIconRes: Int = 0,
        @RawRes soundRes: Int = 0,
        withSound: Boolean = false,
        withCurrentTime: Boolean = false,
    ) {
        val notificationManager = notificationManager
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                notificationManager.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                initTrackingNotificationChannel(context, channelId)
            }
        }
        val notificationBuilder = NotificationCompat.Builder(service, channelId)
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(smallIconRes)
            .setContentInfo(info)

        if (actionBroadcast != null) {
            val stopSelfIntent = Intent(service, service.javaClass).apply {
                action = ACTION_STOP_SERVICE
            }
            val pendingStopSelfIntent = PendingIntent.getService(
                service,
                0,
                stopSelfIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            notificationBuilder.addAction(0, actionText, pendingStopSelfIntent)
        }

        if (withCurrentTime) {
            notificationBuilder.setWhen(System.currentTimeMillis())
        }

        if (withSound) {
            if (soundRes == 0) {
                notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            } else {
                notificationBuilder.setSound(Uri.parse("android.resource://" + service.packageName + "/" + soundRes))
            }
        }
        try {
            val notification = notificationBuilder.build()
            notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
            if (!isNotification) {
                service.startForeground(notificationId, notification)
                isNotification = true
            } else {
                notificationManager?.notify(notificationId, notification)
            }
        } catch (e: NullPointerException) {
            Timber.e(e, "Cannot build notification - error")
            // https://stackoverflow.com/questions/43123466/java-lang-nullpointerexception-attempt-to-invoke-interface-method-java-util-it
            // This is OS error, seems to be fixed in Android 7+
            Handler(Looper.getMainLooper()).postDelayed({
                setForegroundNotification(
                    context = context,
                    channelId = channelId,
                    title = title,
                    description = description,
                    info = info,
                    actionText = actionText,
                    actionBroadcast = actionBroadcast,
                    smallIconRes = smallIconRes,
                    soundRes = soundRes,
                    withSound = withSound,
                    withCurrentTime = withCurrentTime,
                )
            }, 200)
        }
    }

    fun removeNotification() {
        notificationManager?.cancel(notificationId)
    }

    private val notificationManager: NotificationManager?
        get() = service.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
    }
}

internal fun initTrackingNotificationChannel(context: Context, channelId: String) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
    }
    val notificationManager =
        context.getSystemService(NOTIFICATION_SERVICE) as? NotificationManager ?: return
    val notificationChannel =
        notificationManager.getNotificationChannel(channelId)
    if (notificationChannel == null) {
        val channel = NotificationChannel(
            channelId,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = context.getString(R.string.notification_channel_description)
        notificationManager.createNotificationChannel(channel)
    }
}

fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (manager != null) {
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    }
    return false
}

fun isActivityRunning(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    if (manager != null) {
        @Suppress("DEPRECATION")
        for (task in manager.getRunningTasks(Int.MAX_VALUE)) {
            if (context.packageName == task.baseActivity?.packageName) {
                return true
            }
        }
    }
    return false
}
