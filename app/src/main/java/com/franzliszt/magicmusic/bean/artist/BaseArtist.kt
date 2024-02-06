package com.franzliszt.magicmusic.bean.artist

data class BaseArtist(
    val code:Int,
    val more:Boolean,
    val artists:List<Artist>
)
