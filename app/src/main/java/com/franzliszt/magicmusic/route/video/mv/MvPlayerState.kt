package com.franzliszt.magicmusic.route.video.mv

import com.franzliszt.magicmusic.bean.mv.MvBean
import com.franzliszt.magicmusic.route.playlist.NetworkStatus

data class MvPlayerUIState(
    val id:Long = 0L,
    val url:String = "",
    val isFavorite:Boolean = false,
    val mvInfo:MvBean? = null,
    val isVisibility:Boolean = false,
    val isPlaying:Boolean = false,
    val isFullScreen:Boolean = false,
    val progress:Float = 0f,
    val currentPosition:Long = 0L,
    val similarMvs:List<MvBean> = emptyList(),
    val similarState:NetworkStatus = NetworkStatus.Waiting
)

sealed class MvPlayerEvent{
    object Favorite:MvPlayerEvent()
    object PlayOrPause:MvPlayerEvent()
    object ShowControlPanel:MvPlayerEvent()
    object FullScreen:MvPlayerEvent()
    data class ChangeProgress(val progress: Float):MvPlayerEvent()
}
sealed class MvPlayerState{
    data class Message(val msg:String):MvPlayerState()
    object OpenComment:MvPlayerState()
}