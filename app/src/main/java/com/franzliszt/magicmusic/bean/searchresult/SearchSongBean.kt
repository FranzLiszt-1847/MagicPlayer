package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class SearchSongBean(
    val searchQcReminder: Any,
    val songCount: Int,
    val songs: List<DailySong>
)