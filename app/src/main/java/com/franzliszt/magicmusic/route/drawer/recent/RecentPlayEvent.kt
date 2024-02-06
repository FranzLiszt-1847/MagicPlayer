package com.franzliszt.magicmusic.route.drawer.recent

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

sealed class RecentPlayEvent {
    data class PlaySong(val index:Int):RecentPlayEvent()
}