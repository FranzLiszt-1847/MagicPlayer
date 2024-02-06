package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.playlist.Playlist

data class SearchPlaylistBean(
    val searchQcReminder:Any,
    val playlists:List<Playlist>,
    val playlistCount:Int
)


