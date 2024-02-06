package com.franzliszt.magicmusic.route.nav.recommend.songs

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.recommend.newsongs.RecommendNewSongsBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.franzliszt.magicmusic.bean.recommend.newsongs.Result
import com.franzliszt.magicmusic.route.nav.recommend.playlist.RecommendPlaylistStickyHeader
import com.franzliszt.magicmusic.tool.Loading
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecommendSongsPage(
    onSongItem:(Long)->Unit,
    viewModel: SongViewModel = hiltViewModel()
){
    val value = viewModel.uiStatus.value
    val headers = remember { SongTab.values() }
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(key1 = scaffoldState.snackbarHostState){
        viewModel.eventFlow.collectLatest {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) { data->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                .background(color = MagicMusicTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ){
            if (value.newSongs.isEmpty() || value.daySongs.isEmpty()){
                item { Loading() }
            }
            headers.forEach { header->
                stickyHeader {
                    RecommendPlaylistStickyHeader(header = header.msg)
                }
                when(header){
                    SongTab.Daily->{
                        item{
                            RecommendSongs(bean = value.newSongs, onSongItem = {
                                viewModel.onEvent(SongEvent.InsertNewSong(it))
                                onSongItem(it.id)
                            })
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    SongTab.New->{
                        items(value.daySongs.size){
                            EveryDaySongsItem(bean = value.daySongs[it],onSongItem = { song->
                                viewModel.onEvent(SongEvent.InsertDaySong(song))
                                onSongItem(song.id)
                            })
                            if (it < value.daySongs.size-1)
                                Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendSongs(
    onSongItem: (Result) -> Unit,
    bean: List<Result>
){
    LazyRow(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(MagicMusicTheme.colors.background)
    ){
        items(bean.size){
            RecommendSongsItem(bean = bean[it],onSongItem = onSongItem)
            if (it < bean.size-1)
                Spacer(modifier = Modifier.width(15.dp))
        }
    }
}

@Composable
private fun RecommendSongsItem(
    bean:Result,
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    maxWidth:Int = LocalConfiguration.current.screenWidthDp,
    onSongItem: (Result) -> Unit
){
    Box(
        modifier = Modifier
            .height((maxHeight / 3).dp)
            .width((maxWidth / 2 - maxWidth / 4).dp)
            .background(MagicMusicTheme.colors.background)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSongItem(bean) }
    ){
        AsyncImage(
            model = bean.picUrl,
            contentDescription = bean.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier.fillMaxSize()
        )
        Surface(
            color = MagicMusicTheme.colors.background,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd)
                .padding(start = 5.dp, end = 5.dp, bottom = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MagicMusicTheme.colors.textTitle,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EveryDaySongsItem(
    bean: DailySong,
    onSongItem: (DailySong) -> Unit,
){
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(MagicMusicTheme.colors.background)
        .clickable { onSongItem(bean) }
    ) {
        val (pic,title,num,more) = createRefs()
        AsyncImage(
            model = bean.al.picUrl,
            contentDescription = bean.al.name,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(pic) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )

        //歌曲名字
        Text(
            text = bean.name,
            style = MaterialTheme.typography.body2,
            color = MagicMusicTheme.colors.textTitle,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(title){
                top.linkTo(pic.top,2.dp)
                start.linkTo(pic.end,10.dp)
                end.linkTo(more.start)
                width = Dimension.fillToConstraints
            }
        )

        //歌手名称
        Text(
            text = bean.ar[0].name,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(num){
                bottom.linkTo(pic.bottom,2.dp)
                start.linkTo(pic.end,10.dp)
                end.linkTo(more.start)
                width = Dimension.fillToConstraints
            }
        )

        Icon(
            painter = painterResource(id = R.drawable.icon_more),
            contentDescription = stringResource(id = R.string.more),
            tint = MagicMusicTheme.colors.textTitle,
            modifier = Modifier
                .size(24.dp)
                .constrainAs(more) {
                    top.linkTo(title.top)
                    bottom.linkTo(num.bottom)
                    end.linkTo(parent.end, 5.dp)
                }
        )
    }
}

private enum class SongTab(val msg:String){
    Daily("Daily Songs"),
    New("New"),
}