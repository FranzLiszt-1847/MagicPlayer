package com.franzliszt.magicmusic.route.playlist

sealed class PlaylistStatus {
    data class TransformResult(val msg:String):PlaylistStatus()
    data class NetworkFailed(val msg:String):PlaylistStatus()
    data class Without(val msg:String):PlaylistStatus()
    data class CommentResult(val msg:String):PlaylistStatus()
    object OpenComment:PlaylistStatus()
}

/**
 * 网络请求状态*/
sealed class NetworkStatus{
    object Waiting:NetworkStatus()
    object Successful:NetworkStatus()
    data class Failed(val error:String):NetworkStatus()
}

sealed class BottomSheetScreen{
    object PlaylistComments:BottomSheetScreen()
    object FloorComments:BottomSheetScreen()
    object Playlist:BottomSheetScreen()
}