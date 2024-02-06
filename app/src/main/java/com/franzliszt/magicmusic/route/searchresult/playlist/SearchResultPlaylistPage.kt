package com.franzliszt.magicmusic.route.searchresult.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.route.searchresult.SearchResultItem
import com.franzliszt.magicmusic.route.searchresult.SearchResultUserItem
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme

@Composable
fun SearchResultPlaylistList(
    keyword:String,
    viewModel: SearchResultPlaylistViewModel = hiltViewModel(),
    onPlaylist:(Long)->Unit
){
    val playlists = viewModel.getSearchPlaylistResult(keyword).collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        when(playlists.loadState.refresh){
            is LoadState.Loading-> { item { Loading() } }
            is LoadState.Error-> { item { LoadingFailed{ playlists.retry() } } }
            else ->{}
        }
        items(items = playlists){playlist->
            if (playlist != null) {
                SearchResultItem(
                    cover = playlist.coverImgUrl,
                    nickname = playlist.name,
                    author = playlist.creator.nickname,
                    onClick = { onPlaylist(playlist.id) }
                )
            }
        }
        when(playlists.loadState.append){
            is LoadState.Loading-> { item { Loading() } }
            else ->{}
        }
    }
}