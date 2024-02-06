package com.franzliszt.magicmusic.bean.recommend.songs

data class RecommendSongsBean(
    val dailySongs: List<DailySong>,
    val mvResourceInfos: Any,
    val orderSongs: List<Any>,
    val recommendReasons: List<Any>
)