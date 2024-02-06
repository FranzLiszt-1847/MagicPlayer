package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.artist.Artist

data class SearchArtistBean(
    val artistCount: Int,
    val artists: List<Artist>,
    val searchQcReminder: Any
)