package com.franzliszt.magicmusic.bean.recent

import com.franzliszt.magicmusic.bean.artist.Artist

data class RecentVideoBean(
    val id:Long,
    val idStr:String,
    val name:String,
    val artist: String,
    val cover:String,
    val duration:Int,
    val tag:String
)
