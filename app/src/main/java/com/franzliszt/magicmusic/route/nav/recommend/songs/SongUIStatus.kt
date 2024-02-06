package com.franzliszt.magicmusic.route.nav.recommend.songs

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class SongUIStatus(
    val newSongs:List<com.franzliszt.magicmusic.bean.recommend.newsongs.Result> = emptyList(),
    val daySongs:List<DailySong> = emptyList()
)
