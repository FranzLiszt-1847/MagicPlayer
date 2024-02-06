package com.franzliszt.magicmusic.bean.recent

data class RecentPlayBean<T>(
    val banned: Boolean,
    val `data`: T,
    val multiTerminalInfo: Any,
    val playTime: Long,
    val resourceId: String,
    val resourceType: String,
    val os:String
)