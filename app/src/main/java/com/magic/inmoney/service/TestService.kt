package com.magic.inmoney.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.Nullable
import com.magic.inmoney.R
import io.reactivex.disposables.Disposable

class TestService : Service() {

    override fun onCreate() {
        super.onCreate()

        foregroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable?.dispose()
    }

    companion object {

        var mDisposable: Disposable? = null
    }

    private fun foregroundNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "channel_1"
        val channel: NotificationChannel?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = NotificationChannel(channelId, "channel_log",
                    NotificationManager.IMPORTANCE_DEFAULT)

            channel.enableLights(false)
            channel.enableVibration(false)
            channel.vibrationPattern = longArrayOf(0)
            channel.setSound(null, null)

            notificationManager.createNotificationChannel(channel)

            startForeground(280, Notification.Builder(this)
                    .setChannelId(channelId)
                    .setSmallIcon(R.mipmap.icon)
                    .setContent(RemoteViews(packageName, R.layout.notifition_lay)).build())
        }
    }
}
