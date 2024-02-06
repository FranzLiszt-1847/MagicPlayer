package com.franzliszt.magicmusic.route.nav.recommend.albums

import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.albums.DigitAlbumsBean

data class AlbumUIStatus(
    val albums:List<AlbumsBean> = emptyList(),
    val digitAlbums:List<DigitAlbumsBean> = emptyList(),
    val albumsRank:List<DigitAlbumsBean> = emptyList()
)
