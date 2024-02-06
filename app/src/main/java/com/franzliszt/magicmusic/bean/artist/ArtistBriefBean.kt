package com.franzliszt.magicmusic.bean.artist

import com.franzliszt.magicmusic.bean.user.UserProfileBean

data class ArtistBriefBean(
    val artist: ArtistInfoBean,
    val blacklist: Boolean,
    val identify: Any,
    val preferShow: Int,
    val secondaryExpertIdentiy: List<Any>,
    val showPriMsg: Boolean,
    val videoCount: Int,
    val eventCount:Int,
    val user:UserProfileBean
)