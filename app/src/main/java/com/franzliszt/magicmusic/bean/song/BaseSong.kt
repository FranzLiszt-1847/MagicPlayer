package com.franzliszt.magicmusic.bean.song

import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class BaseSong<T>(
    val code: Int,
    val privileges: Any,
    val songs: T
)