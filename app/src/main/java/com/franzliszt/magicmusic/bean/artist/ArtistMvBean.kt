package com.franzliszt.magicmusic.bean.artist

import com.franzliszt.magicmusic.bean.mv.MvInfoBean

data class ArtistMvBean(
    val code: Int,
    val hasMore: Boolean,
    val mvs: List<MvInfoBean>,
    val time: Long
)