package com.franzliszt.magicmusic.route.video.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.route.musicplayer.notification.MusicNotificationAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoNotificationManager(
    private val context: Context,
    private val exoPlayer: ExoPlayer
) {
    private val NOTIFICATION_ID = 3
    private val NOTIFICATION_CHANNEL_NAME = "Video Notification channel"
    private val NOTIFICATION_CHANNEL_ID = "Video Notification channel id"

    private var  notificationManager = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession,
    ){
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundNotificationService(mediaSessionService: MediaSessionService){
        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(mediaSession: MediaSession){
        PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        ).setMediaDescriptionAdapter(
            MusicNotificationAdapter(
                context = context,
                pendingIntent = mediaSession.sessionActivity
            )
        )
            .setSmallIconResourceId(R.drawable.magicmusic_logo) //通知栏的小图标
            .build()
            .apply {
                setMediaSessionToken(mediaSession.sessionCompatToken)
                setUseFastForwardActionInCompactView(true)
                setUseRewindActionInCompactView(true)
                setUseNextActionInCompactView(true)
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
                setPlayer(exoPlayer)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun closeNotification(){
        notificationManager.cancel(NOTIFICATION_ID)
    }
}