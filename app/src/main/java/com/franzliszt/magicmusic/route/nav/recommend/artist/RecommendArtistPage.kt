package com.franzliszt.magicmusic.route.nav.recommend.artist

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.route.nav.recommend.playlist.RecommendPlaylistStickyHeader
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RecommendArtistPage(
    viewModel: ArtistViewModel = hiltViewModel(),
    onArtist:(Long)->Unit
){
    val state = viewModel.getArtists().collectAsLazyPagingItems()
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(scaffoldState.snackbarHostState ){
       viewModel.eventFlow.collectLatest {
           when(it){
               ArtistStatus.Retry->{
                   scope.launch {
                       val result = scaffoldState.snackbarHostState.showSnackbar(
                           message = it.status,
                           actionLabel ="Retry",
                           duration = SnackbarDuration.Short,
                       )
                       /**
                        * 如果点击了Snack的Retry按钮*/
                       if (result == SnackbarResult.ActionPerformed){
                           state.retry()
                       }
                   }
               }

               ArtistStatus.Finish->{
                  scaffoldState.snackbarHostState.showSnackbar(
                       message = it.status
                   )
               }
           }
       }
    }
    Scaffold(
        scaffoldState = scaffoldState
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                .background(color = MagicMusicTheme.colors.background),
        ){
            item(span = { GridItemSpan(this.maxLineSpan) }) {
                //ArtistStickyHeader()
                RecommendPlaylistStickyHeader(stringResource(id = R.string.artist))
            }

            when(state.loadState.refresh){
                is LoadState.Loading->{
                    item(span = { GridItemSpan(this.maxLineSpan) }) { Loading() } }

                is LoadState.Error->{
                    item(span = { GridItemSpan(this.maxLineSpan) }) { LoadingFailed(){} }
                    viewModel.onEvent(ArtistEvent.Retry)
                }
                else ->{}
            }
            items(items = state){ item->
                item?.let {
                    ArtistListItem(item,onArtist = onArtist)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            when(state.loadState.append){
                is LoadState.Loading-> {

                }
                is LoadState.Error-> {
                    item(span = { GridItemSpan(this.maxLineSpan) }) { LoadingFailed(){} }
                    viewModel.onEvent(ArtistEvent.Retry)
                }
                 else->{}
            }
        }
    }
}

@Composable
private fun ArtistStickyHeader(){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(id = R.string.artist),
            style = MaterialTheme.typography.h6,
            color = MagicMusicTheme.colors.textTitle
        )
        Icon(
            painter = painterResource(id = R.drawable.icon_filter),
            contentDescription = "filter",
            tint = MagicMusicTheme.colors.textContent,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ArtistListItem(
    bean: Artist,
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    onArtist:(Long)->Unit
) {
    Column (
        modifier = Modifier
            .height((maxHeight / 4).dp)
            .clickable { onArtist(bean.id) }
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.Center
    ){
        AsyncImage(
            model = bean.picUrl,
            contentDescription = bean.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = bean.name,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
/**
 * LazyVerticalGrid还不支持Paging3分页管理库，通过此扩展函数可以实现*/
fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    span: ((item: T) -> GridItemSpan)? = null,
    contentType: ((item: T) -> Any)? = null,
    itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
    items(
        count = items.itemCount,
        key = if (key == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                //PagingPlaceholderKey(index)
            } else {
                key(item)
            }
        },
        span = if (span == null) null else { index ->
            val item = items.peek(index)
            if (item == null) {
                GridItemSpan(1)
            } else {
                span(item)
            }
        },
        contentType = if (contentType == null) {
            { null }
        } else { index ->
            val item = items.peek(index)
            if (item == null) {
                null
            } else {
                contentType(item)
            }
        }
    ) { index ->
        itemContent(items[index])
    }
}