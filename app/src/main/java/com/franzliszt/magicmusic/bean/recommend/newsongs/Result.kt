package com.franzliszt.magicmusic.bean.recommend.newsongs

import com.franzliszt.magicmusic.bean.song.NewSongBean

data class Result(
    val alg: String,
    val canDislike: Boolean,
    val copywriter: Any,
    val id: Long,
    val name: String,
    val picUrl: String,
    val song: NewSongBean,
    val trackNumberUpdateTime: Any,
    val type: Int
)