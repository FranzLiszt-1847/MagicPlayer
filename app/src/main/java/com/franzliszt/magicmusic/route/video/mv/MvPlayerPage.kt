package com.franzliszt.magicmusic.route.video.mv

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.artist.ArtistInfoBean
import com.franzliszt.magicmusic.bean.mv.MvBean
import com.franzliszt.magicmusic.route.playlist.BottomSheetScreen
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.playlist.PlaylistStatus
import com.franzliszt.magicmusic.route.searchresult.SearchResultVideoItem
import com.franzliszt.magicmusic.route.video.mlog.FullScreenControl
import com.franzliszt.magicmusic.route.video.mlog.MlogPlayerEvent
import com.franzliszt.magicmusic.tool.CommentBottomSheet
import com.franzliszt.magicmusic.tool.FloorCommentBottomSheet
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MvPlayerPage(
     viewModel: MvPlayerViewModel = hiltViewModel(),
     onItemMv:(Long)->Unit,
     onBack: () -> Unit
){
    val value = viewModel.uiState.value
    val commentValue = viewModel.commentStatus.value
    val scaffoldState = rememberBottomSheetScaffoldState()
    val commentListState = rememberLazyListState()
    val floorCommentListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val playerHeight = (LocalConfiguration.current.screenHeightDp / 3f).dp
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

    val lifecycle = remember { mutableStateOf(Lifecycle.Event.ON_CREATE) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle.value = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = scaffoldState.snackbarHostState){
        viewModel.eventFlow.collectLatest {
            when(it){
                is MvPlayerState.Message-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is MvPlayerState.OpenComment->  scaffoldState.bottomSheetState.expand()
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
                    viewModel.onEvent(PlaylistEvent.NextCommentPage)
                }
            }
    }

    LaunchedEffect(key1 = floorCommentLoadMore ){
        //floor comment
        snapshotFlow{ floorCommentLoadMore.value }
            .distinctUntilChanged()
            .collect{
                if (it){
                    viewModel.onEvent(PlaylistEvent.NextFloorCommentPage)
                }
            }
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
            when (viewModel.bottomSheetScreen.value) {
                is BottomSheetScreen.PlaylistComments -> {
                    CommentBottomSheet(
                        state = commentListState,
                        text = commentValue.commentText,
                        status = commentValue.commentStatus,
                        commentCount = commentValue.commentCount,
                        cover = value.mvInfo?.cover ?: "",
                        name = value.mvInfo?.name ?: "Unknown",
                        artist = value.mvInfo?.artistName ?: "Unknown",
                        comments = viewModel.comments,
                        onAgreeComment = { id, index ->
                            viewModel.onEvent(PlaylistEvent.AgreeComment(id, index, false))
                        },
                        onFloorComment = { id, index ->
                            viewModel.onEvent(PlaylistEvent.OpenFloorComment(id, index))
                        },
                        onSend = { viewModel.onEvent(PlaylistEvent.SendComment) },
                        onValueChange = { viewModel.onEvent(PlaylistEvent.ChangeComment(it)) }
                    )
                }

                is BottomSheetScreen.FloorComments -> {
                    FloorCommentBottomSheet(
                        state = floorCommentListState,
                        text = commentValue.floorCommentText,
                        status = commentValue.floorCommentStatus,
                        commentCount = commentValue.floorCommentCount,
                        ownComment = commentValue.ownFloorComment,
                        comments = viewModel.floorComments,
                        onAgreeComment = { id, index ->
                            viewModel.onEvent(PlaylistEvent.AgreeComment(id, index, true))
                        },
                        onSend = { viewModel.onEvent(PlaylistEvent.SendFloorComment(it)) },
                        onValueChange = { viewModel.onEvent(PlaylistEvent.ChangeFloorComment(it)) }
                    )
                }

                else -> {}
            }
        },
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
    ){
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ){
            val (playerRes,controlRes,similarRes) = createRefs()
            AndroidView(
                factory = { context->
                    PlayerView(context).apply {
                        viewModel.mediaController.value
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = {
                    if (it.player == null)it.player = viewModel.mediaController.value
                    when(lifecycle.value){
                        Lifecycle.Event.ON_STOP-> {
                            it.onPause()
                            it.player?.stop()
                        }
                        Lifecycle.Event.ON_PAUSE-> {
                            it.onPause()
                            it.player?.pause()
                        }
                        Lifecycle.Event.ON_RESUME-> it.onResume()

                        else-> Unit
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .clickable { viewModel.onPlayEvent(MvPlayerEvent.ShowControlPanel) }
                    .background(MagicMusicTheme.colors.black)
                    .constrainAs(playerRes){
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
            )
                PlayerControls(
                    isPlaying = value.isPlaying,
                    isVisible = value.isVisibility && !value.isFullScreen,
                    progress = value.progress,
                    currentPosition = value.currentPosition,
                    bean = value.mvInfo,
                    onBack = onBack,
                    onChangeProgress = { viewModel.onPlayEvent(MvPlayerEvent.ChangeProgress(it)) },
                    onPlayOrPause = { viewModel.onPlayEvent(MvPlayerEvent.PlayOrPause) },
                    onFullScreen = { viewModel.onPlayEvent(MvPlayerEvent.FullScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(controlRes){
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(playerRes.top)
                            bottom.linkTo(playerRes.bottom)
                        }
                )

            AnimatedVisibility(
                visible = !value.isFullScreen,
                enter = EnterTransition.None,
                exit = ExitTransition.None,
                modifier = Modifier.constrainAs(similarRes){
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(playerRes.bottom)
                }
            ){
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 10.dp, top = 5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MagicMusicTheme.colors.background)
                ){
                    item {
                        Text(
                            text = stringResource(id = R.string.information),
                            style = MaterialTheme.typography.h6,
                            color = MagicMusicTheme.colors.textTitle
                        )
                    }
                    item {
                        MvDetailInfo(value.mvInfo)
                    }
                    item {
                        MvCountInfo(
                            isFavorite = value.isFavorite,
                            bean = value.mvInfo,
                            onShare = {},
                            onComment = { viewModel.onEvent(PlaylistEvent.OpenPlaylistComment) },
                            onFavorite = { viewModel.onPlayEvent(MvPlayerEvent.Favorite) }
                        )
                    }
                    item {
                        Text(
                            text = stringResource(id = R.string.similar),
                            style = MaterialTheme.typography.h6,
                            color = MagicMusicTheme.colors.textTitle
                        )
                    }
                    when(value.similarState){
                        is NetworkStatus.Waiting-> item { Loading() }
                        is NetworkStatus.Failed-> item { LoadingFailed(content = value.similarState.error) {} }
                        is NetworkStatus.Successful->{
                            if (value.similarMvs.isEmpty()){
                                item { LoadingFailed(content = stringResource(id = R.string.not_similar_mv)) {} }
                            }
                        }
                    }
                    items(value.similarMvs){ mv->
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
            }

            AnimatedVisibility(
                visible = value.isFullScreen && value.isVisibility,
                enter = EnterTransition.None,
                exit = ExitTransition.None,
            ) {
                if (value.mvInfo != null){
                    FullScreenControl(
                        progress = value.progress,
                        currentPosition = value.currentPosition,
                        title = value.mvInfo.name,
                        duration = value.mvInfo.duration.toLong(),
                        isPlaying = value.isPlaying,
                        onExitFullScreen = { viewModel.onPlayEvent(MvPlayerEvent.FullScreen) },
                        onPlayOrPause = { viewModel.onPlayEvent(MvPlayerEvent.PlayOrPause) },
                        onChangeProgress = { viewModel.onPlayEvent(MvPlayerEvent.ChangeProgress(it))  },
                        onShowControl = { viewModel.onPlayEvent(MvPlayerEvent.ShowControlPanel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MvDetailInfo(
    bean: MvBean?,
    maxHeight:Dp = 70.dp
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        AsyncImage(
            model = bean?.cover ?: "",
            contentDescription = "Cover",
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .width(maxHeight.times(0.7f))
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = bean?.name ?: "Unknown",
                style = MaterialTheme.typography.body1,
                color = MagicMusicTheme.colors.textTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = bean?.artistName ?: "Unknown",
                style = MaterialTheme.typography.caption,
                color = MagicMusicTheme.colors.textContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MvCountInfo(
    isFavorite:Boolean,
    bean: MvBean?,
    onShare:()->Unit,
    onComment:()->Unit,
    onFavorite:()->Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MagicMusicTheme.colors.grayBackground)
            .padding(5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_share),
                contentDescription = "Share",
                tint = MagicMusicTheme.colors.textContent,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onShare() }
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_comment),
                contentDescription = "Comment",
                tint = MagicMusicTheme.colors.textContent,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onComment() }
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_follow),
                contentDescription = "Favorite",
                tint = if (isFavorite) MagicMusicTheme.colors.highlightColor else MagicMusicTheme.colors.textContent,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onFavorite() }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${bean?.shareCount ?: "Share"}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${bean?.commentCount ?: "Comment"}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${bean?.subCount ?: "Favorite"}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun PlayerControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    isVisible:Boolean,
    progress: Float,
    currentPosition:Long,
    bean: MvBean?,
    onBack: () -> Unit,
    onChangeProgress: (Float) -> Unit,
    onPlayOrPause: () -> Unit,
    onFullScreen: () -> Unit
){
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier
    )
    {
        if (bean != null){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 5.dp, end = 5.dp, top = 20.dp, bottom = 20.dp)
            ){
                TopControl(
                    title = bean.name,
                    onBack = onBack,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                BottomControl(
                    isPlaying = isPlaying,
                    progress = progress,
                    currentPosition = currentPosition,
                    duration = bean.duration.toLong(),
                    onChangeProgress = onChangeProgress,
                    onPlayOrPause = onPlayOrPause,
                    onFullScreen = onFullScreen,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

/**
 * 播放器顶部标题栏*/
@Composable
fun TopControl(
    modifier: Modifier = Modifier,
    title:String,
    onBack:()->Unit
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft, 
            contentDescription = "Back",
            tint = MagicMusicTheme.colors.white,
            modifier = Modifier
                .size(24.dp)
                .clickable { onBack() }
        )
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            color = MagicMusicTheme.colors.white,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BottomControl(
    modifier: Modifier = Modifier,
    isPlaying:Boolean,
    isFullScreen:Boolean = false,
    progress:Float,
    currentPosition:Long,
    duration:Long,
    onChangeProgress:(Float)->Unit,
    onPlayOrPause:()->Unit,
    onFullScreen:()->Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        //播放或暂停按钮
        Icon(
            painter = painterResource(id = if (isPlaying) R.drawable.icon_play else R.drawable.icon_stop),
            contentDescription = "Back",
            tint = MagicMusicTheme.colors.white,
            modifier = Modifier
                .size(24.dp)
                .clickable { onPlayOrPause() }
        )
        Spacer(modifier = Modifier.width(10.dp))
        //进度条
        androidx.compose.material.Slider(
            valueRange = 0f..100f,
            value = progress,
            onValueChange = { onChangeProgress(it) },
            colors = androidx.compose.material.SliderDefaults.colors(
                thumbColor = MagicMusicTheme.colors.highlightColor,
                activeTrackColor = MagicMusicTheme.colors.selectIcon,
                inactiveTrackColor = MagicMusicTheme.colors.unselectIcon
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(5.dp))
        //当前进度
        Text(
            text = currentPosition.formatMinSec(),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Start,
            color = MagicMusicTheme.colors.white,
        )
        Text(
            text = "/",
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Start,
            color = MagicMusicTheme.colors.white,
        )
        //视频总时长
        Text(
            text = duration.formatMinSec(),
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Start,
            color = MagicMusicTheme.colors.white,
        )
        Spacer(modifier = Modifier.width(10.dp))
        //全屏按钮
        Icon(
            painter = painterResource(id = if (isFullScreen)R.drawable.icon_exit_full_screen else R.drawable.icon_into_full_screen ),
            contentDescription = "Full screen",
            tint = MagicMusicTheme.colors.white,
            modifier = Modifier
                .size(24.dp)
                .clickable { onFullScreen() }
        )
    }
}

private fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "..."
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}