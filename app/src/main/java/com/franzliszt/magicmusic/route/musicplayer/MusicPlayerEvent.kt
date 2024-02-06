package com.franzliszt.magicmusic.route.musicplayer

import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent

sealed class MusicPlayerEvent{
    data class ProgressChange(val progress:Float):MusicPlayerEvent()
    data class DurationChange(val duration:Long):MusicPlayerEvent()
    data class ChangePlayMedia(val index:Int):MusicPlayerEvent()
    data class DeleteSong(val bean: SongMediaBean):MusicPlayerEvent()
    object ChangePlayStatus:MusicPlayerEvent()
    object Prior:MusicPlayerEvent()
    object Next:MusicPlayerEvent()
    object BottomSheet:MusicPlayerEvent()

}
