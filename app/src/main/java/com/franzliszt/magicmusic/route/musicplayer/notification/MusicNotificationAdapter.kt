package com.franzliszt.magicmusic.route.musicplayer.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.NonCancellable.cancel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@UnstableApi
class MusicNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?,
):PlayerNotificationManager.MediaDescriptionAdapter {
    /**
     * 通知栏中歌曲的封面、名称、作者等信息*/
    override fun getCurrentContentTitle(player: Player): CharSequence {
        return player.mediaMetadata.title ?: "Unknown"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    override fun getCurrentContentText(player: Player): CharSequence {
       return player.mediaMetadata.subtitle ?: "Unknown"
    }


    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        getBitmap(
            url = player.mediaMetadata.artworkUri, //此字段内容为约定而使
            onSuccess = {
                callback.onBitmap(it)
            },
            onError = {

            }
        )
        return null
    }

    @OptIn(DelicateCoroutinesApi::class)
     private fun getBitmap(
        url:Uri?,
        onSuccess:(Bitmap)->Unit,
        onError:(String)->Unit
    ){
        var bitmap:Bitmap? = null
        val scope = GlobalScope.launch(Dispatchers.Main){
            val request = ImageRequest.Builder(context = context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            if (result is SuccessResult){
                bitmap =  (result.drawable as BitmapDrawable).bitmap
            }else{
                cancel("Error Request")
            }
        }
        scope.invokeOnCompletion {
            bitmap?.let { bitmap->
                onSuccess(bitmap)
            }?:it?.let {
                onError(it.message.toString())
            }?: onError("Unknown Exception")
        }
    }
}