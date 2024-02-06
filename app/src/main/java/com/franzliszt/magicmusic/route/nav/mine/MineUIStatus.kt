package com.franzliszt.magicmusic.route.nav.mine

import com.franzliszt.magicmusic.bean.banner.BannerBean
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.route.playlist.NetworkStatus

data class MineUIStatus(
    var banners:List<BannerBean> = emptyList(),
    var creates:MutableList<Playlist> = mutableListOf(),
    var favorites:MutableList<Playlist> = mutableListOf(),
    var preferBean:Playlist? = null,
    var mapPlaylist:MutableMap<String,Boolean> = mutableMapOf(),
    val playlistState:NetworkStatus = NetworkStatus.Waiting
)
