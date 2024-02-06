package com.franzliszt.magicmusic.route.drawer.download

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.route.drawer.playback.PlayBackEvent
import com.franzliszt.magicmusic.route.drawer.recent.IconTopTitleBar
import com.franzliszt.magicmusic.route.drawer.recent.TopTitleBar
import com.franzliszt.magicmusic.route.nav.recommend.MusicTabRow
import com.franzliszt.magicmusic.tool.CustomAlertDialog
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DownloadMusicPage(
    viewModel: DownloadViewModel = hiltViewModel(),
    onBack:()->Unit,
    onSongItem:(Long)->Unit
){
    val dialogValue = viewModel.dialogState.value
    val tabs = remember { DownloadTab.values().map { it.tab } }
    val pageState = androidx.compose.foundation.pager.rememberPagerState{ tabs.size }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MagicMusicTheme.colors.background)
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ){
        IconTopTitleBar(onBack = onBack, title = "Download", icon = R.drawable.icon_clear){
            //清空所有下载记录,包括已经下载完成和未下载完成的
            viewModel.onEvent(DownloadEvent.ShowDialog)
        }
        MusicTabRow(pagerState = pageState,tabs = tabs)
        HorizontalPager(state =pageState ) {index->
            when(tabs[index]){
                DownloadTab.Downloaded.tab->{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ){
                        if (viewModel.downloadList.none { it.download }){
                            item { LoadingFailed(content = "No songs have been downloaded yet") {} }
                        }
                        items(viewModel.downloadList.filter { it.download }){bean->
                            downloadedMusicItem(bean = bean, onDownload = {
                                viewModel.onEvent(DownloadEvent.PlayLocalMusic(bean = bean))
                                onSongItem(bean.musicID)
                            })
                        }
                    }
                }
                DownloadTab.Downloading.tab->{
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ){
                        if (viewModel.downloadList.none { !it.download }){
                            item { LoadingFailed(content = "There are currently no songs being downloaded") {} }
                        }
                        items(viewModel.downloadList.filter { !it.download }){bean->
                            downloadingMusicItem(bean = bean, onDownload = { viewModel.onEvent(DownloadEvent.PlayOrPause(bean)) })
                        }
                    }
                }
            }
        }
        CustomAlertDialog(
            visibility = dialogValue.isVisibility,
            title = dialogValue.title,
            content = dialogValue.content,
            confirmText = dialogValue.confirmBtn,
            cancelText = dialogValue.cancelBtn,
            onConfirm = { viewModel.onEvent(DownloadEvent.DialogConfirm) },
            onCancel = { viewModel.onEvent(DownloadEvent.DialogCancel) },
            onDismiss = { viewModel.onEvent(DownloadEvent.DialogCancel) }
        )
    }
}

@Composable
private fun downloadingMusicItem(
    bean:DownloadMusicBean,
    height:Dp = 70.dp,
    coverWidth:Dp = (LocalConfiguration.current.screenWidthDp/3).dp,
    onDownload: () -> Unit
){
    val animatedProgress = animateFloatAsState(
        targetValue = bean.progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "animatedProgress"
    )
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .background(MagicMusicTheme.colors.background)
        .clickable { onDownload() }
    ) {
        val (cover,name,artist,progress,speed,size) = createRefs()
        AsyncImage(
            model = bean.cover,
            contentDescription = bean.musicName,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(coverWidth)
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(cover) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )

        //歌曲名字
        Text(
            text = bean.musicName,
            style = MaterialTheme.typography.body2,
            color = MagicMusicTheme.colors.textTitle,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(name){
                top.linkTo(parent.top,5.dp)
                start.linkTo(cover.end,5.dp)
                end.linkTo(artist.start)
                width = Dimension.fillToConstraints
            }
        )

        //歌手名称
        Text(
            text = bean.artist,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(artist){
                top.linkTo(name.top)
                end.linkTo(parent.end)
            }
        )

        //进度条
        LinearProgressIndicator(
            progress = animatedProgress.value,
            color = bean.progressColor,
            trackColor = MagicMusicTheme.colors.progressTrackColor,
            modifier = Modifier.constrainAs(progress){
                start.linkTo(cover.end,5.dp)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            }.clip(RoundedCornerShape(10.dp))
        )

        //下载速度
        Text(
            text = bean.speed,
            style = MaterialTheme.typography.caption,
            color = bean.progressColor,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(speed){
                bottom.linkTo(parent.bottom,5.dp)
                start.linkTo(cover.end,5.dp)
            }
        )

        //资源大小
        Text(
            text = bean.size,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(size){
                bottom.linkTo(parent.bottom,5.dp)
                end.linkTo(parent.end)
            }
        )
    }
}

@Composable
private fun downloadedMusicItem(
    bean:DownloadMusicBean,
    height:Dp = 70.dp,
    coverWidth:Dp = (LocalConfiguration.current.screenWidthDp/3).dp,
    onDownload:()->Unit
){
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .background(MagicMusicTheme.colors.background)
        .clickable { onDownload ()}
    ) {
        val (cover,name,artist,size) = createRefs()
        AsyncImage(
            model = bean.cover,
            contentDescription = bean.musicName,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(coverWidth)
                .height(height)
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(cover) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )

        //歌曲名字
        Text(
            text = bean.musicName,
            style = MaterialTheme.typography.body2,
            color = MagicMusicTheme.colors.textTitle,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(name){
                top.linkTo(cover.top,5.dp)
                start.linkTo(cover.end,5.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        //歌手名称
        Text(
            text = bean.artist,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(artist){
                top.linkTo(name.bottom,5.dp)
                start.linkTo(cover.end,5.dp)
                end.linkTo(size.start,5.dp)
                width = Dimension.fillToConstraints
            }
        )

        //资源大小
        Text(
            text = bean.size,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.constrainAs(size){
                top.linkTo(name.bottom,5.dp)
                end.linkTo(parent.end)
            }
        )
    }
}

private enum class DownloadTab(val tab:String){
    Downloading("Downloading"),
    Downloaded("Downloaded")
}