package com.franzliszt.magicmusic.bean.song

import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.artist.Artist

data class SongBean(
    val album: AlbumsBean,
    val alias: List<Any>,
    val artists: List<Artist>,
    val copyrightId: Int,
    val duration: Int,
    val fee: Int,
    val ftype: Int,
    val id: Int,
    val mark: Long,
    val mvid: Int,
    val name: String,
    val rUrl: Any,
    val rtype: Int,
    val status: Int
)