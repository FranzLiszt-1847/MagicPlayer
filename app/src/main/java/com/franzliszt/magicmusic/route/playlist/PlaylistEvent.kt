package com.franzliszt.magicmusic.route.playlist

sealed class PlaylistEvent {
    object IsShowDialog:PlaylistEvent()
    object SavePhoto:PlaylistEvent()
    object NextCommentPage:PlaylistEvent()
    object OpenPlaylistComment:PlaylistEvent()
    object SendComment:PlaylistEvent()
    object NextFloorCommentPage:PlaylistEvent()
    data class PlayMusicItem(val index: Int,val id: Long):PlaylistEvent()
    data class SendFloorComment(val commentID:Long):PlaylistEvent()
    data class AgreeComment(val id:Long,val index:Int,val isFloor:Boolean):PlaylistEvent()
    data class OpenFloorComment(val id:Long,val index:Int):PlaylistEvent()
    data class ChangeComment(val msg:String):PlaylistEvent()
    data class ChangeFloorComment(val msg:String):PlaylistEvent()
}