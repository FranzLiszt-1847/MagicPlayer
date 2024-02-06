package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.video.VideoBean

data class SearchVideoBean(
    val searchQcReminder: Any,
    val videoCount: Int,
    val videos: List<VideoBean>
)