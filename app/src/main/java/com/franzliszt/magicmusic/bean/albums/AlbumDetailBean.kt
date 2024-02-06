package com.franzliszt.magicmusic.bean.albums

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class AlbumDetailBean(
    val album: AlbumsBean,
    val code: Int,
    val resourceState: Boolean,
    val songs: List<DailySong>
)