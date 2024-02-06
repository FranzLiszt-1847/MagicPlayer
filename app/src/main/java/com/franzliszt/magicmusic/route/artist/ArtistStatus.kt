package com.franzliszt.magicmusic.route.artist

import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.bean.artist.ArtistInfoBean
import com.franzliszt.magicmusic.bean.mv.MvInfoBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class ArtistUIStatus(
    val isFollow:Boolean = false,
    val artist:ArtistInfoBean? = null,
    val songs:List<DailySong> = emptyList(),
    val albums:List<AlbumsBean> = emptyList(),
    val mvs:List<MvInfoBean> = emptyList(),
    val similar:List<Artist> = emptyList()
)

sealed class ArtistStatus{
    data class NetworkFailed(val msg:String):ArtistStatus()
}