package com.kartik.contactapplicationkotlin.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kartik.contactapplicationkotlin.R


/**
 * Make an notification Utility class
 * This will create notification and it will also handle notification channels for oreo and above supported devices
 * */
class NotificationUtility {
    companion object {
        private fun initNotification(
            pContext: Context,
            pString: String? = pContext.getString(R.string.app_name),
            pTitle: String
        ): NotificationCompat.Builder {
            val appName = pContext.resources.getString(R.string.app_name)
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(pContext, pContext.packageName, pContext.packageName)
            } else {
                Constants.sEmpty_String
            }
            val sound = try {
                //Define sound URI
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            } catch (exception: Exception) {
                null
            }

            return NotificationCompat.Builder(pContext, channel)
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker(appName)
                .setContentTitle(pTitle)
                .setContentText(pString).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        priority = NotificationManager.IMPORTANCE_HIGH
                    }
                }
                .setVibrate(LongArray(0))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .apply {
                    if (sound != null) {
                        setSound(sound)
                    }
                }
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setColor(ContextCompat.getColor(pContext, R.color.colorAccent))
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun createNotificationChannel(
            pContext: Context,
            channelId: String,
            channelName: String
        ): String {
            val chan = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            )
            chan.lightColor = Color.BLUE
            chan.enableVibration(true)
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service =
                pContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
        }

        fun displayNotification(
            pContext: Context,
            pNotificationId: Int,
            pMessage: String?,
            pTitle: String,
            intent: Intent
        ) {
            val notification = initNotification(pContext, pMessage, pTitle)

            val pendingIntent =
                PendingIntent.getActivity(pContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            notification.setContentIntent(pendingIntent)

            val notificationManager =
                pContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(pNotificationId, notification.build())
        }
    }
}