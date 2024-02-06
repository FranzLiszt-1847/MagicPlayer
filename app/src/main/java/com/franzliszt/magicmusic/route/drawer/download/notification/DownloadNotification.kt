package com.franzliszt.magicmusic.route.drawer.download.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import com.franzliszt.magicmusic.R

@RequiresApi(Build.VERSION_CODES.O)
class DownloadNotification(
    private val context:Context
) {
    private val NOTIFICATION_CHANNEL_NAME = "Download Notification channel"
    private val NOTIFICATION_CHANNEL_ID = "Download Notification channel id"

    private lateinit var notificationBuilder:NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManagerCompat

    private val maxProgress = 100


    fun createNotification(id:Int,name: String,bitmap: Bitmap):Notification?{
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager = NotificationManagerCompat.from(context)
            notificationBuilder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID.plus(id))
            createNotificationChannel(id)
            return startNotification(id,name, bitmap)
        }
        return null
    }
    @OptIn(UnstableApi::class)
    private fun startNotification(id: Int,name: String,bitmap: Bitmap):Notification?{
        notificationBuilder
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.magicmusic_logo)
            .setAutoCancel(false)
            .setProgress(maxProgress,0,false)
            .setContentText(name)
            .setLargeIcon(bitmap)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        notificationManager.notify(id,notificationBuilder.build())
        return notificationBuilder.build()
    }


     fun setProgress(id:Int,progress:Int){
        if (this::notificationBuilder.isInitialized){
            if (progress in 0 until maxProgress){
                notificationBuilder.setContentText("${progress}% downloaded")
                notificationBuilder.setProgress(maxProgress,progress,false)
            }else if (progress == maxProgress){
                notificationBuilder.setContentText("downloaded successful!")
                notificationBuilder.setAutoCancel(true)
            }else{
                notificationBuilder.setContentText("downloaded failed!")
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            notificationManager.notify(id,notificationBuilder.build())
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(id:Int){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID.plus(id),
            NOTIFICATION_CHANNEL_NAME.plus(id),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }
}