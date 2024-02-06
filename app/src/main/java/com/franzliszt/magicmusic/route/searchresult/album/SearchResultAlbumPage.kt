package com.franzliszt.magicmusic.route.searchresult.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import androidx.paging.compose.itemsIndexed
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.route.searchresult.SearchResultItem
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme

@Composable
fun SearchResultAlbumList(
    keyword:String,
    viewModel: SearchResultAlbumViewModel = hiltViewModel(),
    onAlbum:(Long)->Unit
){
    val albums = viewModel.getSearchAlbumResult(keyword).collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        when(albums.loadState.refresh){
            is LoadState.Loading-> { item { Loading() } }
            is LoadState.Error-> { item { LoadingFailed{ albums.retry() } } }
            else ->{}
        }
        items(items = albums){album->
            if (album != null) {
                SearchResultItem(
                    cover = album.blurPicUrl,
                    nickname = album.name,
                    author = album.artist.name,
                    onClick = { onAlbum(album.id) }
                )
            }
        }
        when(albums.loadState.append){
            is LoadState.Loading-> { item { Loading() } }
            else ->{}
        }
    }
}