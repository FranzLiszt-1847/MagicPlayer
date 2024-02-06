package com.franzliszt.magicmusic.bean.lrc

/**
 * 歌词中作者、作词等信息*/
data class LyricAuthorBean(
    val c: List<C>,//信息
    val t: Long //为信息出现时间
)