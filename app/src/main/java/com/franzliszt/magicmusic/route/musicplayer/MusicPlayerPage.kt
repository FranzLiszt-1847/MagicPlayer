package com.franzliszt.magicmusic.route.musicplayer

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.lrc.LyricBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.route.playlist.BottomSheetScreen
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.searchresult.transformTime
import com.franzliszt.magicmusic.tool.CommentBottomSheet
import com.franzliszt.magicmusic.tool.FloorCommentBottomSheet
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn( ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MusicPlayerPage(
    viewModel: MusicPlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val value = viewModel.uiStatus.value
    val commentValue = viewModel.commentStatus.value
    val tabs = remember { MusicPlayerInterface.values() }
    val pagerState = androidx.compose.foundation.pager.rememberPagerState( pageCount = { tabs.size })
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val commentListState = rememberLazyListState()
    val floorCommentListState = rememberLazyListState()
    val lazyListState = rememberLazyListState()
    /**
     * 判断列表是否滑动到了最后一项*/
    val commentLoadMore = remember {
        derivedStateOf {
            commentListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == commentListState.layoutInfo.totalItemsCount-1
        }
    }
    val floorCommentLoadMore = remember {
        derivedStateOf {
            floorCommentListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == floorCommentListState.layoutInfo.totalItemsCount-1
        }
    }
    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest {
            scope.launch {
                when (it) {
                    is MusicPlayerStatus.BottomSheet -> {
                        scaffoldState.bottomSheetState.expand()
                    }
                    is MusicPlayerStatus.NetworkFailed->{
                        scaffoldState.snackbarHostState.showSnackbar(it.msg)
                    }

                    is MusicPlayerStatus.Message->{
                        scaffoldState.snackbarHostState.showSnackbar(it.msg)
                    }
                }
            }
        }
    }

    /**
     * 如果列表滑动到了最后一项则发起网络请求，加载更多*/
    LaunchedEffect(key1 = commentLoadMore){
        //comment
        snapshotFlow{ commentLoadMore.value }
            .distinctUntilChanged()
            .collect{
                if (it){
                    viewModel.onCommentEvent(PlaylistEvent.NextCommentPage)
                }
            }
    }

    LaunchedEffect(key1 = floorCommentLoadMore ){
        //floor comment
        snapshotFlow{ floorCommentLoadMore.value }
            .distinctUntilChanged()
            .collect{
                if (it){
                    viewModel.onCommentEvent(PlaylistEvent.NextFloorCommentPage)
                }
            }
    }


    LaunchedEffect(key1 = value.currentLine) {
        lazyListState.animateScrollToItem(value.currentLine)
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        },
        sheetContent = {
            when(viewModel.bottomSheetScreen.value){
                is BottomSheetScreen.PlaylistComments->{
                    CommentBottomSheet(
                        state = commentListState,
                        text = commentValue.commentText,
                        status = commentValue.commentStatus,
                        commentCount = commentValue.commentCount,
                        cover = value.cover,
                        name = value.name,
                        artist = value.artist,
                        comments = commentValue.comments,
                        onAgreeComment = { id, index ->
                            viewModel.onCommentEvent(PlaylistEvent.AgreeComment(id, index,false))
                        },
                        onFloorComment = { id, index ->
                            viewModel.onCommentEvent(PlaylistEvent.OpenFloorComment(id, index))
                        },
                        onSend = { viewModel.onCommentEvent(PlaylistEvent.SendComment) },
                        onValueChange = { viewModel.onCommentEvent(PlaylistEvent.ChangeComment(it)) }
                    )
                }

                is BottomSheetScreen.FloorComments->{
                    FloorCommentBottomSheet(
                        state = floorCommentListState,
                        text = commentValue.floorCommentText,
                        status = commentValue.floorCommentStatus,
                        commentCount = commentValue.floorCommentCount,
                        ownComment = commentValue.ownFloorComment,
                        comments = commentValue.floorComments,
                        onAgreeComment =  { id, index ->
                            viewModel.onCommentEvent(PlaylistEvent.AgreeComment(id, index,true))
                        },
                        onSend = { viewModel.onCommentEvent(PlaylistEvent.SendFloorComment(it)) },
                        onValueChange = { viewModel.onCommentEvent(PlaylistEvent.ChangeFloorComment(it)) }
                    )
                }

                is BottomSheetScreen.Playlist->{
                    BottomSheetLayout(
                        curSongID = value.musicID,
                        playlist = value.playlist,
                        onDeleteItem = { viewModel.onEvent(MusicPlayerEvent.DeleteSong(it)) },
                        onPlayItem = { viewModel.onEvent(MusicPlayerEvent.ChangePlayMedia(it)) }
                    )
                }
            } },
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetBackgroundColor = MagicMusicTheme.colors.background,
        sheetPeekHeight = 0.dp,
        modifier = Modifier
            .pointerInput(Unit) {
                //点击BottomSheet以外的区域，则关闭当前sheet
                detectTapGestures(onTap = {
                    scope.launch {
                        if (scaffoldState.bottomSheetState.isExpanded)
                            scaffoldState.bottomSheetState.collapse()
                    }
                })
            }
            .fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()){
            AsyncImage(
                model = value.cover,
                contentDescription = "background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur((LocalConfiguration.current.screenHeightDp / 2).dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MagicMusicTheme.colors.textContent,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBack() }
                    )
                    AnimatedVisibility(
                        visible = pagerState.currentPage == 1
                    ) {
                        Text(
                            text = "${value.name}-${value.artist}",
                            color = MagicMusicTheme.colors.textContent,
                            style = MaterialTheme.typography.caption,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(end = 24.dp)
                                .fillMaxWidth()
                        )
                    }
                }
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = pagerState
                    ) { it ->
                        when (tabs[it]) {
                            MusicPlayerInterface.Cover -> {
                                CoverInterface(
                                    cover = value.cover,
                                    artist = value.artist,
                                    name = value.name,
                                    duration = value.currentDuration,
                                    total = value.totalDuration,
                                    isPlaying = value.isPlaying,
                                    progress = value.progress,
                                    onValueChange = {
                                        viewModel.onEvent(
                                            MusicPlayerEvent.ProgressChange(
                                                it
                                            )
                                        )
                                    },
                                    onPlayListener = { viewModel.onEvent(MusicPlayerEvent.ChangePlayStatus) },
                                    onPrior = { viewModel.onEvent(MusicPlayerEvent.Prior) },
                                    onNext = { viewModel.onEvent(MusicPlayerEvent.Next) },
                                    onPlaylist = { viewModel.onEvent(MusicPlayerEvent.BottomSheet) },
                                    onCommend = { viewModel.onCommentEvent(PlaylistEvent.OpenPlaylistComment) }
                                )
                            }

                            MusicPlayerInterface.Lyric -> {
                                LyricInterface(
                                    state = lazyListState,
                                    line = value.currentLine,
                                    lyrics = value.lyrics
                                ) { duration ->
                                    //点击某一行歌词，并将音频进度调整至此
                                    viewModel.onEvent(MusicPlayerEvent.DurationChange(duration))
                                }
                            }
                        }
                }

            }
        }
    }
}

@Composable
private fun CoverInterface(
    cover: String,
    artist: String,
    name: String,
    progress: Float,
    duration: String,
    total: String,
    isPlaying: Boolean,
    onValueChange: (Float) -> Unit,
    onPlayListener: () -> Unit,
    onPrior: () -> Unit,
    onNext: () -> Unit,
    onPlaylist: () -> Unit,
    onCommend: () -> Unit
) {
    //唱片旋转角度
    val coverRotation = infiniteRotation(isPlaying)
    //唱针旋转角度
    val stylusRotation by animateFloatAsState(targetValue = if (isPlaying) 0f else -45f, label = "")
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.4f)
        ) {
            val (coverRes, stylusRes) = createRefs()
            Card(
                shape = CircleShape,
                border = BorderStroke(
                    width = 20.dp,
                    MagicMusicTheme.colors.textTitle.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .constrainAs(coverRes) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }
                    .graphicsLayer { rotationZ = coverRotation.value }
            ) {
                //音乐封面
                AsyncImage(
                    model = cover,
                    contentDescription = "Cover",
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.magicmusic_logo),
                    modifier = Modifier
                        .size((LocalConfiguration.current.screenHeightDp / 3).dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.stylus),
                contentDescription = "stylus",
                modifier = Modifier
                    .size(128.dp)
                    .constrainAs(stylusRes) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
                    .graphicsLayer {
                        rotationZ = stylusRotation
                    }
            )
        }
        Column(
            modifier = Modifier
                .weight(0.9f)
                .padding(top = 10.dp)
        ) {
            //歌曲名称
            Text(
                text = name,
                color = MagicMusicTheme.colors.white,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6.copy(fontFamily = FontFamily(Font(R.font.zhimangxing_regular))),
                modifier = Modifier.fillMaxWidth()
            )
            //作者名称
            Text(
                text = artist,
                color = MagicMusicTheme.colors.textContent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp, start = 40.dp, end = 40.dp)
            ) {
                //收藏
                Icon(
                    painter = painterResource(id = R.drawable.icon_follow),
                    contentDescription = "Favorite",
                    tint = MagicMusicTheme.colors.unselectIcon,
                    modifier = Modifier.size(32.dp)
                )
                //歌曲评论
                Icon(
                    painter = painterResource(id = R.drawable.icon_comment),
                    contentDescription = "Comment",
                    tint = MagicMusicTheme.colors.unselectIcon,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onCommend() }
                )
                //播放列表
                Icon(
                    painter = painterResource(id = R.drawable.icon_playlist_song),
                    contentDescription = "Playlist",
                    tint = MagicMusicTheme.colors.unselectIcon,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onPlaylist() }
                )
            }
            androidx.compose.material.Slider(
                valueRange = 0f..100f,
                value = progress,
                onValueChange = onValueChange,
                colors = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = MagicMusicTheme.colors.highlightColor,
                    activeTrackColor = MagicMusicTheme.colors.selectIcon,
                    inactiveTrackColor = MagicMusicTheme.colors.unselectIcon
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
//            Slider(
//                valueRange = 0f..100f,
//                value = progress,
//                onValueChange = onValueChange,
//                track = { sliderPositions ->
//                    SliderDefaults.Track(
//                        modifier = Modifier.scale(scaleX = 1f, scaleY = 1.8f),
//                        sliderPositions = sliderPositions
//                    )
//                },
//                colors = SliderDefaults.colors(
//                    thumbColor = MagicMusicTheme.colors.highlightColor,
//                    activeTrackColor = MagicMusicTheme.colors.selectIcon,
//                    inactiveTrackColor = MagicMusicTheme.colors.unselectIcon
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp)
            ) {
                //当前时间
                Text(
                    text = duration,
                    color = MagicMusicTheme.colors.textContent,
                    style = MaterialTheme.typography.caption
                )
                //总时间
                Text(
                    text = total,
                    color = MagicMusicTheme.colors.textContent,
                    style = MaterialTheme.typography.caption,
                )
            }
        }
        /**
         * 上一首、播放/暂停、下一首*/
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .weight(0.7f)

        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_prior),
                contentDescription = "prior",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onPrior() }
            )
            Icon(
                painter = painterResource(
                    id =
                    if (isPlaying) R.drawable.icon_play
                    else R.drawable.icon_stop
                ),
                contentDescription = "play",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(38.dp)
                    .clickable { onPlayListener() }
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_next),
                contentDescription = "next",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onNext() }
            )
        }
    }
}

@Composable
private fun LyricInterface(
    state:LazyListState = rememberLazyListState(),
    line: Int,
    lyrics: List<LyricBean>,
    onClickLyric: (Long) -> Unit
) {
    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
    ) {
        itemsIndexed(lyrics) { index, bean ->
            Text(
                text = bean.text,
                color = if (index == line) MagicMusicTheme.colors.white else MagicMusicTheme.colors.textContent,
                style = if (index == line) MaterialTheme.typography.h6.copy(fontFamily = FontFamily.Default)
                else MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickLyric(bean.time) }
            )
        }
    }
}

@Composable
private fun BottomSheetLayout(
    curSongID: Long,
    playlist: List<SongMediaBean>,
    onPlayItem:(Int)->Unit,
    onDeleteItem:(SongMediaBean)->Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height((LocalConfiguration.current.screenHeightDp * 0.7f).dp)
            .padding(20.dp)
            .navigationBarsPadding()
    ) {
        item {
            Text(
                text = "Playlist",
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
                color = MagicMusicTheme.colors.textTitle,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (playlist.isEmpty()) {
            item { LoadingFailed(content = "No playback items!") {} }
        }
        itemsIndexed(playlist) { index, item ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (curSongID == item.songID)
                            MagicMusicTheme.colors.textContent.copy(alpha = 0.3f)
                        else
                            MagicMusicTheme.colors.background
                    )
                    .padding(10.dp)
                    .clickable { onPlayItem(index) }
            ) {
                Text(
                    text = item.songName,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body1,
                    color = if (curSongID == item.songID)
                        MagicMusicTheme.colors.selectIcon
                    else
                        MagicMusicTheme.colors.textTitle
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = item.artist,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.caption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (curSongID == item.songID)
                        MagicMusicTheme.colors.selectIcon
                    else
                        MagicMusicTheme.colors.textContent,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete",
                    tint = MagicMusicTheme.colors.textContent,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDeleteItem(item) }
                )
            }
        }
    }
}

/**
 * 无限循环的旋转动画
 */
@Composable
private fun infiniteRotation(
    isPlaying: Boolean,
    durationMillis: Int = 8 * 1000
): Animatable<Float, AnimationVector1D> {
    var rotation by remember { mutableStateOf(Animatable(0f)) }
    LaunchedEffect(key1 = isPlaying) {
        if (isPlaying) {
            //开启无限旋转
            rotation.animateTo(
                targetValue = (rotation.value % 360f) + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durationMillis,
                        easing = LinearEasing
                    )
                )
            )
        } else {
            //关闭旋转动画
            rotation.stop()
            rotation = Animatable(rotation.value % 360f)
        }
    }
    return rotation
}

private enum class MusicPlayerInterface {
    Cover,
    Lyric
}
