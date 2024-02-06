package com.franzliszt.magicmusic.route.drawer.playback

import com.franzliszt.magicmusic.bean.song.SongMediaBean

data class PlayBackUIState(
    val isVisibility:Boolean = false,
    val title:String = "System prompt",
    val content:String = "",
    val confirmBtn:String = "Confirm",
    val cancelBtn:String = "Cancel"
)

sealed class PlayBackState{
    data class Message(val msg:String):PlayBackState()
    data class Authorized(val msg:String):PlayBackState()
    object Permission:PlayBackState()
}
sealed class PlayBackEvent{
    data class Download(val bean:SongMediaBean):PlayBackEvent()
    data class DeleteItem(val bean:SongMediaBean):PlayBackEvent()
    data class PlayItem(val index:Int):PlayBackEvent()
    object ConfirmDelete:PlayBackEvent()
    object CancelDelete:PlayBackEvent()
    object ApplicationPermission:PlayBackEvent()
}