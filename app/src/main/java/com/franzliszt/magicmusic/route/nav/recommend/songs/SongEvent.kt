package com.franzliszt.magicmusic.route.nav.recommend.songs

import com.franzliszt.magicmusic.bean.recommend.newsongs.Result
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

sealed class SongEvent {
    data class InsertDaySong(val bean:DailySong):SongEvent()
    data class InsertNewSong(val bean:Result):SongEvent()
}