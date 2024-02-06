package com.franzliszt.magicmusic.route.nav.recommend.playlist

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.recommend.dayplaylist.RecommendDayPlaylistBean
import com.franzliszt.magicmusic.bean.recommend.playlist.Result
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecommendPlaylistPage(
    viewModel: RePlaylistViewModel = hiltViewModel(),
    onPlaylist: (id:Long) -> Unit
){
    val value =viewModel.uiStatus.value
    val headers = remember { PlaylistTab.values() }

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
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MagicMusicTheme.colors.background)
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                .background(color = MagicMusicTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ){
            if (value.newPlaylist.isEmpty() || value.dayPlaylist.isEmpty()){
                item { Loading() }
            }
            headers.forEach { header->
                stickyHeader {
                    RecommendPlaylistStickyHeader(header = header.msg)
                    Spacer(modifier = Modifier.height(5.dp))
                }
                when(header){
                    PlaylistTab.Daily->{
                        item{
                            RecommendPlaylist(value.newPlaylist){
                                onPlaylist(it.id)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    PlaylistTab.New->{
                        items(value.dayPlaylist.size){
                            EveryDayPlaylistItem(value.dayPlaylist[it]){ playlist->
                                onPlaylist(playlist.id)
                            }
                            if (it < value.dayPlaylist.size-1)
                                Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendPlaylistStickyHeader(
    header: String,
    style: TextStyle = MaterialTheme.typography.h6
) {
    Text(
        text = header,
        style = style,
        color = MagicMusicTheme.colors.textTitle
    )
}

@Composable
private fun RecommendPlaylist(
    bean: List<Result>,
    onPlaylist:(Result)->Unit
){
    LazyRow(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.background(MagicMusicTheme.colors.background)
    ){
        items(bean.size){
            RecommendPlaylistItem(bean[it], onPlaylist = onPlaylist)
            if (it < bean.size-1)
                Spacer(modifier = Modifier.width(15.dp))
        }
    }
}

@Composable
private fun RecommendPlaylistItem(
    bean: Result,
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    maxWidth:Int = LocalConfiguration.current.screenWidthDp,
    onPlaylist:(Result)->Unit
){
    Box(
        modifier = Modifier
            .height((maxHeight / 4).dp)
            .width((maxWidth / 2).dp)
            .background(MagicMusicTheme.colors.background)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onPlaylist(bean) }
    ){
        AsyncImage(
            model = bean.picUrl,
            contentDescription = bean.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 5.dp, end = 5.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            //歌单名称
            Text(
                text = bean.name,
                style =  MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                color = MagicMusicTheme.colors.textContent,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = MagicMusicTheme.colors.background,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MagicMusicTheme.colors.textTitle,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                ConstraintLayout(modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MagicMusicTheme.colors.background.copy(alpha = 0.5f))
                ) {
                    val (playIcon,playNum,splitLine,songsIcon,songsNum) = createRefs()
                    Divider(modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .constrainAs(splitLine) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top, 10.dp)
                            bottom.linkTo(parent.bottom, 10.dp)
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MagicMusicTheme.colors.background,
                        modifier = Modifier
                            .size(24.dp)
                            .constrainAs(playIcon) {
                                start.linkTo(parent.start)
                                end.linkTo(splitLine.start)
                                top.linkTo(parent.top, (-10).dp)
                                bottom.linkTo(parent.bottom)
                            }
                    )
                    Text(
                        text = transformNum(bean.playCount),
                        fontSize = 10.sp,
                        color = MagicMusicTheme.colors.textTitle,
                        modifier = Modifier.constrainAs(playNum){
                            top.linkTo(playIcon.bottom)
                            start.linkTo(playIcon.start)
                            end.linkTo(playIcon.end)
                        }
                    )

                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Songs",
                        tint = MagicMusicTheme.colors.background,
                        modifier = Modifier
                            .size(24.dp)
                            .constrainAs(songsIcon) {
                                start.linkTo(splitLine.end)
                                end.linkTo(parent.end)
                                top.linkTo(parent.top, (-10).dp)
                                bottom.linkTo(parent.bottom)
                            }
                    )
                    Text(
                        text = transformNum(bean.trackCount.toLong()),
                        fontSize = 10.sp,
                        color = MagicMusicTheme.colors.textTitle,
                        modifier = Modifier.constrainAs(songsNum){
                            top.linkTo(songsIcon.bottom)
                            start.linkTo(songsIcon.start)
                            end.linkTo(songsIcon.end)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EveryDayPlaylistItem(
    bean: RecommendDayPlaylistBean,
    onPlaylist: (RecommendDayPlaylistBean) -> Unit
){
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(MagicMusicTheme.colors.background)
        .clickable { onPlaylist(bean) }
    ) {
        val (pic,title,num,more) = createRefs()
        AsyncImage(
            model = bean.picUrl,
            contentDescription = bean.name,
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

        Text(
            text = bean.name,
            style = MaterialTheme.typography.body2,
            color = MagicMusicTheme.colors.textTitle,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(title){
                top.linkTo(pic.top,2.dp)
                start.linkTo(pic.end,10.dp)
                end.linkTo(more.start)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = "${bean.trackCount} Songs",
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(num){
                bottom.linkTo(pic.bottom,2.dp)
                start.linkTo(pic.end,10.dp)
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

 fun transformNum(num:Long):String{
    if (num < 10000)return "$num"
    return "${remainDigit(num / 10000.0)}W"
}


/**
 * 保留二位小数*/
fun remainDigit(num: Double):String {
    val df = DecimalFormat("#.00")
    return df.format(num)
}

private enum class PlaylistTab(val msg:String){
    Daily("Daily Playlist"),
    New("New"),
}