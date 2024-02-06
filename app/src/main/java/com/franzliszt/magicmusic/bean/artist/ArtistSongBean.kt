package com.franzliszt.magicmusic.bean.artist

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class ArtistSongBean(
    val artist: Artist,
    val code: Int,
    val hotSongs: List<DailySong>,
    val more: Boolean
)