package com.franzliszt.magicmusic.route.nav.recommend.playlist

import com.franzliszt.magicmusic.bean.recommend.dayplaylist.RecommendDayPlaylistBean
import com.franzliszt.magicmusic.bean.recommend.playlist.Result

data class RePlaylistUIStatus(
    val newPlaylist:List<Result> = emptyList(),
    val dayPlaylist:List<RecommendDayPlaylistBean> = emptyList(),
)
