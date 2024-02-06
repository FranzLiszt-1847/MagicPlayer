package com.franzliszt.magicmusic.bean.albums

import com.franzliszt.magicmusic.bean.artist.Artist

data class FavoriteAlbumBean(
    val alias: Any,
    val artists: List<Artist>,
    val id: Long,
    val msg: Any,
    val name: String,
    val picId: Long,
    val picUrl: String,
    val size: Int,
    val subTime: Long,
    val transNames: Any
)