package com.franzliszt.magicmusic.bean.lrc

data class LyricInfoBean(
    val code: Int,
    val klyric: Lrc,
    val lrc: Lrc,
    val qfy: Boolean,
    val romalrc: Lrc,
    val sfy: Boolean,
    val sgc: Boolean,
    val tlyric: Lrc
)