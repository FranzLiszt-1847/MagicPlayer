package com.franzliszt.magicmusic.route.drawer.recent

import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.dj.DjRadioBean
import com.franzliszt.magicmusic.bean.mv.MvBean
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.bean.recent.RecentPlayBean
import com.franzliszt.magicmusic.bean.recent.RecentVideoBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.route.playlist.NetworkStatus

data class RecentPlayUIState(
    val songs:List<RecentPlayBean<DailySong>> = emptyList(),
    val playlists:List<RecentPlayBean<Playlist>> = emptyList(),
    val albums:List<RecentPlayBean<AlbumsBean>> = emptyList(),
    val videos:List<RecentVideoBean> = emptyList(),
    val djs:List<RecentPlayBean<DjRadioBean>> = emptyList(),
    val songState:NetworkStatus = NetworkStatus.Waiting,
    val playlistState:NetworkStatus = NetworkStatus.Waiting,
    val albumState:NetworkStatus = NetworkStatus.Waiting,
    val videoState:NetworkStatus = NetworkStatus.Waiting,
    val djState:NetworkStatus = NetworkStatus.Waiting,
)