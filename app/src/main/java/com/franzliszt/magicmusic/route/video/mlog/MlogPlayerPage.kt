package com.franzliszt.magicmusic.route.video.mlog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.video.MlogInfoBean
import com.franzliszt.magicmusic.bean.video.MlogResourceBean
import com.franzliszt.magicmusic.route.nav.recommend.playlist.transformNum
import com.franzliszt.magicmusic.route.playlist.BottomSheetScreen
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.video.mv.BottomControl
import com.franzliszt.magicmusic.route.video.mv.MvPlayerState
import com.franzliszt.magicmusic.route.video.mv.TopControl
import com.franzliszt.magicmusic.tool.CommentBottomSheet
import com.franzliszt.magicmusic.tool.FloorCommentBottomSheet
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MlogPlayerPage(
    viewModel: MlogPlayerViewModel = hiltViewModel(),
    onBack:()->Unit
){
    val value = viewModel.uiState.value
    val commentValue = viewModel.commentStatus.value
    val scaffoldState = rememberBottomSheetScaffoldState()
    val commentListState = rememberLazyListState()
    val floorCommentListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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
                is MlogPlayerState.Message-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is MlogPlayerState.OpenComment-> scaffoldState.bottomSheetState.expand()
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
                        cover =value.mlogInfo?.resource?.content?.video?.frameUrl ?: "",
                        name = value.mlogInfo?.resource?.content?.title ?: "Unknown",
                        artist = value.mlogInfo?.resource?.profile?.nickname ?: "Unknown",
                        comments = viewModel.comments,
                        onAgreeComment = { id, index ->
                            viewModel.onCommentEvent(PlaylistEvent.AgreeComment(id, index, false))
                        },
                        onFloorComment = { id, index ->
                            viewModel.onCommentEvent(PlaylistEvent.OpenFloorComment(id, index))
                        },
                        onSend = { viewModel.onCommentEvent(PlaylistEvent.SendComment) },
                        onValueChange = { viewModel.onCommentEvent(PlaylistEvent.ChangeComment(it)) }
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
                            viewModel.onCommentEvent(PlaylistEvent.AgreeComment(id, index, true))
                        },
                        onSend = { viewModel.onCommentEvent(PlaylistEvent.SendFloorComment(it)) },
                        onValueChange = { viewModel.onCommentEvent(PlaylistEvent.ChangeFloorComment(it)) }
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
                    .background(MagicMusicTheme.colors.black)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = 10.dp)
            ) {
                val (backRes,controlRes,playerRes,fullRes,mlogInfoRes,mlogCountRes,sliderRes) = createRefs()
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
                        if(it.player == null) it.player = viewModel.mediaController.value
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
                        .clickable { viewModel.onEvent(MlogPlayerEvent.FullScreenControl) }
                        .constrainAs(playerRes) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom, 100.dp)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

                AnimatedVisibility(
                    visible = !value.isFullScreen,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier
                        .constrainAs(backRes) {
                            start.linkTo(parent.start, 20.dp)
                            top.linkTo(parent.top)
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MagicMusicTheme.colors.white,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onBack() }
                    )
                }
                    AnimatedVisibility(
                        visible = !value.isPlaying && !value.isFullScreen,
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                        modifier = Modifier.constrainAs(controlRes){
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(playerRes.top)
                            bottom.linkTo(playerRes.bottom)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = if (value.isPlaying) R.drawable.icon_play else R.drawable.icon_stop),
                            contentDescription = "PlayOrPause",
                            tint = MagicMusicTheme.colors.white,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { viewModel.onEvent(MlogPlayerEvent.PlayOrPause) }
                        )
                    }

                AnimatedVisibility(
                    visible = !value.isFullScreen,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier
                        .constrainAs(fullRes) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(playerRes.bottom, 10.dp)
                        }
                ){
                    //全屏按钮
                    Icon(
                        painter = painterResource(id = R.drawable.icon_into_full_screen),
                        contentDescription = "Full Screen",
                        tint = MagicMusicTheme.colors.white,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { viewModel.onEvent(MlogPlayerEvent.FullScreen) }
                    )
                }
                AnimatedVisibility(
                    visible = !value.isFullScreen,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier
                        .constrainAs(mlogInfoRes){
                            start.linkTo(parent.start,20.dp)
                            end.linkTo(mlogCountRes.start,20.dp)
                            bottom.linkTo(sliderRes.top,10.dp)
                            width = Dimension.fillToConstraints
                        }
                ){
                    //视频文案以及作者信息
                    MlogInfo(
                        cover = value.mlogInfo?.resource?.profile?.avatarUrl ?: "",
                        title = value.mlogInfo?.resource?.content?.title ?: "Unknown",
                        text = value.mlogInfo?.resource?.content?.text ?: "Unknown",
                        author = value.mlogInfo?.resource?.profile?.nickname ?: "Unknown",
                    )
                }
                AnimatedVisibility(
                    visible = !value.isFullScreen,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier
                        .constrainAs(mlogCountRes){
                            end.linkTo(parent.end,20.dp)
                            top.linkTo(playerRes.bottom)
                            bottom.linkTo(sliderRes.top,10.dp)
                            height = Dimension.fillToConstraints
                        },
                ){
                    //视频点赞、评论、分享数据
                    if (value.mlogInfo != null){
                        MlogCountInfo(
                            isFavorite = value.isFavorite,
                            bean = value.mlogInfo.resource,
                            onLike = { viewModel.onEvent(MlogPlayerEvent.Favorite) },
                            onComment = { viewModel.onCommentEvent(PlaylistEvent.OpenPlaylistComment) },
                            onShare = { viewModel.onEvent(MlogPlayerEvent.Share) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !value.isFullScreen,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                    modifier = Modifier
                        .constrainAs(sliderRes) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.fillToConstraints
                        }
                ){
                    //视频播放进度条
                    androidx.compose.material.Slider(
                        valueRange = 0f..100f,
                        value = value.progress,
                        onValueChange = { viewModel.onEvent(MlogPlayerEvent.SlidProgress(it)) },
                        colors = androidx.compose.material.SliderDefaults.colors(
                            thumbColor = MagicMusicTheme.colors.highlightColor,
                            activeTrackColor = MagicMusicTheme.colors.selectIcon,
                            inactiveTrackColor = MagicMusicTheme.colors.unselectIcon
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedVisibility(
                    visible = value.isFullScreen && value.isShowControl,
                    enter = EnterTransition.None,
                    exit = ExitTransition.None,
                ) {
                    if (value.mlogInfo != null){
                        FullScreenControl(
                            progress = value.progress,
                            currentPosition = value.currentPosition,
                            title = value.mlogInfo.resource.content.title,
                            duration = value.mlogInfo.resource.content.video.duration,
                            isPlaying = value.isPlaying,
                            onExitFullScreen = { viewModel.onEvent(MlogPlayerEvent.FullScreen) },
                            onPlayOrPause = { viewModel.onEvent(MlogPlayerEvent.PlayOrPause) },
                            onChangeProgress = { viewModel.onEvent(MlogPlayerEvent.SlidProgress(it))  },
                            onShowControl = { viewModel.onEvent(MlogPlayerEvent.FullScreenControl) }
                        )
                    }
                }
            }
    }
}

@Composable
fun FullScreenControl(
    modifier: Modifier = Modifier,
    progress:Float,
    currentPosition:Long,
    title: String,
    duration:Long,
    isPlaying:Boolean,
    onExitFullScreen:()->Unit,
    onPlayOrPause:()->Unit,
    onChangeProgress:(Float)->Unit,
    onShowControl:()->Unit
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(10.dp)
            .clickable { onShowControl() }
    ) {
        TopControl(
            title = title,
            onBack = onExitFullScreen
        )
        Icon(
            painter = painterResource(id = if (isPlaying) R.drawable.icon_play else R.drawable.icon_stop),
            contentDescription = "PlayOrPause",
            tint = MagicMusicTheme.colors.white,
            modifier = Modifier
                .size(48.dp)
                .clickable { onPlayOrPause() }
                .fillMaxWidth()
                .weight(1f)
        )
        BottomControl(
            isPlaying = isPlaying,
            progress = progress,
            isFullScreen = true,
            currentPosition = currentPosition,
            duration = duration,
            onChangeProgress = onChangeProgress,
            onPlayOrPause = onPlayOrPause,
            onFullScreen = onExitFullScreen
        )
    }
}

@Composable
private fun MlogInfo(
    cover:String,
    title:String,
    text:String,
    author:String,
    modifier: Modifier = Modifier
){
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = modifier.fillMaxWidth()

        ) {
            AsyncImage(
                model = cover,
                contentDescription = "Cover",
                placeholder = painterResource(id = R.drawable.magicmusic_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Text(
                text = author,
                style = MaterialTheme.typography.body1,
                color = MagicMusicTheme.colors.white,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            color = MagicMusicTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MlogCountInfo(
    isFavorite:Boolean,
    bean:MlogResourceBean,
    modifier: Modifier = Modifier,
    onLike:()->Unit,
    onComment:()->Unit,
    onShare:()->Unit,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .wrapContentWidth()
            .fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_follow),
                contentDescription = "Favorite",
                tint = if (isFavorite) MagicMusicTheme.colors.highlightColor else MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onLike() }
            )
            Text(
                text = transformNum(bean.likedCount.toLong()),
                color = MagicMusicTheme.colors.white,
                style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_comment),
                contentDescription = "Comment",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onComment() }
            )
            Text(
                text = transformNum(bean.commentCount.toLong()),
                color = MagicMusicTheme.colors.white,
                style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_share),
                contentDescription = "Share",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onShare() }
            )
            Text(
                text = transformNum(bean.shareCount.toLong()),
                color = MagicMusicTheme.colors.white,
                style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default)
            )
        }
    }
}