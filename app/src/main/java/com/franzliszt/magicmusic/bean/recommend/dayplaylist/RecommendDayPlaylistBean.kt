package com.franzliszt.magicmusic.bean.recommend.dayplaylist

data class RecommendDayPlaylistBean(
    val alg: String,
    val copywriter: String,
    val createTime: Long,
    val creator: Any,
    val id: Long,
    val name: String,
    val picUrl: String,
    val playcount: Long,
    val trackCount: Int,
    val type: Int,
    val userId: Int
)