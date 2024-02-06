package com.franzliszt.magicmusic.route.musicplayer

sealed class MusicPlayerStatus{
    data class NetworkFailed(val msg:String):MusicPlayerStatus()
    data class Message(val msg:String):MusicPlayerStatus()
    object BottomSheet:MusicPlayerStatus()
}
