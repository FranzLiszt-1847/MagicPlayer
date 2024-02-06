package com.franzliszt.magicmusic.route.nav.recommend.artist

import com.franzliszt.magicmusic.bean.artist.Artist

data class ArtistUIStatus(
    val artists:MutableList<Artist> = mutableListOf(),
    val isShowFilter: Boolean = false
)
