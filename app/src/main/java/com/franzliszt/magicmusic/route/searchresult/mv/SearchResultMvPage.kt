package com.franzliszt.magicmusic.route.searchresult.mv

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
import com.franzliszt.magicmusic.route.searchresult.SearchResultVideoItem
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme

@Composable
fun SearchResultMvList(
    keyword:String,
    viewModel: SearchResultMvViewModel = hiltViewModel(),
    onItemMv:(Long)->Unit
){
    val mvs = viewModel.getSearchMvResult(keyword).collectAsLazyPagingItems()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        when(mvs.loadState.refresh){
            is LoadState.Loading-> { item { Loading() } }
            is LoadState.Error-> { item { LoadingFailed{ mvs.retry() } } }
            else ->{}
        }
        items(items = mvs){mv->
            if (mv != null) {
                SearchResultVideoItem(
                    cover = mv.cover,
                    title = mv.name,
                    author = mv.artistName,
                    playTime = mv.playCount,
                    durationTime = mv.duration,
                    onClick = { onItemMv(mv.id) }
                )
            }
        }
        when(mvs.loadState.append){
            is LoadState.Loading-> { item { Loading() } }
            else ->{}
        }
    }
}