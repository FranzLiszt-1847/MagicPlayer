package com.franzliszt.magicmusic.bean.search

import com.franzliszt.magicmusic.bean.albums.AlbumsBean
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.bean.song.SongBean

data class SearchSuggestionBean(
    val albums:List<AlbumsBean>,
    val artists:List<Artist>,
    val songs:List<SongBean>,
    val playlists:List<Playlist>,
    val order:List<String>
)
