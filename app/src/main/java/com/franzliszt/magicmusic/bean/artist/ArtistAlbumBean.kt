package com.franzliszt.magicmusic.bean.artist

import com.franzliszt.magicmusic.bean.albums.AlbumsBean

data class ArtistAlbumBean(
    val artist: Artist,
    val code: Int,
    val hotAlbums: List<AlbumsBean>,
    val more: Boolean
)