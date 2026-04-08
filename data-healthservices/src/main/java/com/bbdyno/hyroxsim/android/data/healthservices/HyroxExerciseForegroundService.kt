//
//  HyroxExerciseForegroundService.kt
//  data-healthservices
//
//  Created by bbdyno on 4/8/26.
//

package com.bbdyno.hyroxsim.android.data.healthservices

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

class HyroxExerciseForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        return START_NOT_STICKY
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hyrox Exercise",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("HYROX workout active")
                .setContentText("Health Services session is running")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("HYROX workout active")
                .setContentText("Health Services session is running")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }

    companion object {
        private const val CHANNEL_ID = "hyrox_exercise"
        private const val NOTIFICATION_ID = 4108
    }
}
