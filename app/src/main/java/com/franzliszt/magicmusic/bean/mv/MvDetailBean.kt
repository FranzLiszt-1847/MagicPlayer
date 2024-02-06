package com.franzliszt.magicmusic.bean.mv

data class MvDetailBean(
    val bufferPic: String,
    val bufferPicFS: String,
    val code: Int,
    val `data`: MvBean,
    val loadingPic: String,
    val loadingPicFS: String,
    val mp: Any,
    val subed: Boolean
)