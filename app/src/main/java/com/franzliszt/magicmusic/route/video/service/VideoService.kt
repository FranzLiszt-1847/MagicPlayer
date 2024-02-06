package com.franzliszt.magicmusic.route.video.service

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.franzliszt.magicmusic.APP


class VideoPlayerService : MediaSessionService() {
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession:MediaSession
    private lateinit var videoNotificationManager:VideoNotificationManager

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(APP.context).build()
        mediaSession = MediaSession.Builder(APP.context,exoPlayer).setId("VideoPlayerService").build()
        videoNotificationManager = VideoNotificationManager(APP.context,exoPlayer)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession  = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            videoNotificationManager.startNotificationService(
                this,
                mediaSession
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaSession.apply {
            if (player.playbackState != Player.STATE_IDLE) {
                player.seekTo(0)
                player.playWhenReady = false
                player.stop()
            }
            player.release()
            release()
        }
        super.onDestroy()
    }
}