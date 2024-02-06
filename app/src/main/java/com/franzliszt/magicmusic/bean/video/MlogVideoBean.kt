package com.franzliszt.magicmusic.bean.video

data class MlogVideoBean(
    val coverDetail: Any,
    val coverUrl: String,
    val duration: Long,
    val frameImage: Any,
    val frameUrl: String,
    val height: Int,
    val playCount: Int,
    val rcmdUrlInfo: MlogUrlBean,
    val urlInfo: MlogUrlBean,
    val urlInfos: List<MlogUrlBean>,
    val videoAreaInfo: Any,
    val videoKey: String,
    val width: Int
)