package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.albums.AlbumsBean

data class SearchAlbumBean(
    val albumCount: Int,
    val albums: List<AlbumsBean>
)