package com.franzliszt.magicmusic.route.playlist

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.route.nav.recommend.playlist.transformNum
import com.franzliszt.magicmusic.tool.CommentBottomSheet
import com.franzliszt.magicmusic.tool.FloorCommentBottomSheet
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@Composable
fun PlaylistPage(
    viewModel: PlaylistViewModel = hiltViewModel(),
    onBack:()->Unit,
    onSongItem: (Long) -> Unit
) {
    val value = viewModel.uiStatus.value
    val commentValue = viewModel.commentStatus.value
    val maxOffset = with(LocalDensity.current) { (viewModel.maxTopBarHeight.roundToPx().toFloat() - viewModel.minTopBarHeight.roundToPx().toFloat()) }
    val offset = remember { mutableStateOf(0f) }
    val infiniteOffset = remember { mutableStateOf(0f) }
    val isShowTitle = remember { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val commentListState = rememberLazyListState()
    val floorCommentListState = rememberLazyListState()
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


    LaunchedEffect(key1 = scaffoldState.snackbarHostState){
        viewModel.eventFlow.collectLatest {
            when(it){
                is PlaylistStatus.TransformResult->{
                    scaffoldState.snackbarHostState.showSnackbar(it.msg)
                }
                is PlaylistStatus.NetworkFailed->{
                    scaffoldState.snackbarHostState.showSnackbar(it.msg)
                }
                is PlaylistStatus.Without->{
                    scaffoldState.snackbarHostState.showSnackbar(it.msg)
                }
                is PlaylistStatus.OpenComment->{
                    scaffoldState.bottomSheetState.expand()
                }
                is PlaylistStatus.CommentResult->{
                    scaffoldState.snackbarHostState.showSnackbar(it.msg)
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

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                val newOffset = offset.value + available.y
                val newInOffset = infiniteOffset.value + available.y
                infiniteOffset.value = newInOffset.coerceIn(-(maxOffset * 1.5f), 0f)
                offset.value = newOffset.coerceIn(-maxOffset, 0f)
                isShowTitle.value = -offset.value.roundToInt() == maxOffset.roundToInt()
                return Offset.Zero
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            androidx.compose.material.SnackbarHost(hostState = it) {data->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
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
                        comments = viewModel.comments,
                        onAgreeComment = { id, index ->
                            viewModel.onEvent(PlaylistEvent.AgreeComment(id, index,false))
                        },
                        onFloorComment = { id, index ->
                            viewModel.onEvent(PlaylistEvent.OpenFloorComment(id, index))
                        },
                        onSend = { viewModel.onEvent(PlaylistEvent.SendComment) },
                        onValueChange = { viewModel.onEvent(PlaylistEvent.ChangeComment(it)) }
                    )
                }

                is BottomSheetScreen.FloorComments->{
                    FloorCommentBottomSheet(
                        state = floorCommentListState,
                        text = commentValue.floorCommentText,
                        status = commentValue.floorCommentStatus,
                        commentCount = commentValue.floorCommentCount,
                        ownComment = commentValue.ownFloorComment,
                        comments = viewModel.floorComments,
                        onAgreeComment =  { id, index ->
                            viewModel.onEvent(PlaylistEvent.AgreeComment(id, index,true))
                        },
                        onSend = { viewModel.onEvent(PlaylistEvent.SendFloorComment(it)) },
                        onValueChange = { viewModel.onEvent(PlaylistEvent.ChangeFloorComment(it)) }
                    )
                }
                else ->{}
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
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.background)
                .nestedScroll(nestedScrollConnection)
                .navigationBarsPadding()
        ) {

            PlaylistList(
                isPlaylist = value.isPlaylist,
                maxTopBarHeight = viewModel.maxTopBarHeight,
                songs = value.songs,
                onSongItem = {index,id->
                    viewModel.onEvent(PlaylistEvent.PlayMusicItem(index, id))
                    onSongItem(id)
                }
            )

            FlexibleTopBar(
                title = if (value.isPlaylist) "Playlist" else "Album",
                isShowTitle = isShowTitle,
                cover = value.cover,
                albumName = value.name,
                expendHeight = viewModel.maxTopBarHeight,
                collapseHeight = viewModel.minTopBarHeight,
                offset = offset,
                onBack = onBack
            )

            PlaylistInfo(
                maxHeight = viewModel.maxTopBarHeight,
                minHeight = viewModel.minTopBarHeight,
                cover = value.cover,
                name = value.name,
                artist = value.artist,
                description = value.description,
                shareCount = value.shareCount,
                commentCount = value.commentCount,
                favoriteCount = value.favoriteCount,
                isFollow = value.isFollow,
                offset = infiniteOffset,
                onShowDialog = { viewModel.onEvent(PlaylistEvent.IsShowDialog) },
                onComment = { viewModel.onEvent(PlaylistEvent.OpenPlaylistComment) }
            )

            PlaylistDialog(
                cover = value.cover,
                name = value.name,
                tags = value.tags,
                company = value.company,
                subType = value.type,
                description = value.description,
                isPlaylist = value.isPlaylist,
                isShowDialog = value.isShowDialog,
                onDismiss = {  viewModel.onEvent(PlaylistEvent.IsShowDialog) },
                onSave = { viewModel.onEvent(PlaylistEvent.SavePhoto) }
            )
        }
    }
}

@Composable
private fun PlaylistList(
    isPlaylist: Boolean,
    maxTopBarHeight: Dp,
    songs: List<DailySong>,
    onSongItem: (Int,Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .clip(RoundedCornerShape(topEnd = 40.dp, topStart = 40.dp))
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = maxTopBarHeight + (10).dp)
    ) {
        //数据未加载完成时，显示loading
        if (songs.isEmpty()){
            item { Loading() }
        }
        if (isPlaylist) {
            //playlist
            itemsIndexed(songs) { index, item ->
                PlaylistSongsItem(bean = item, onSongItem = { onSongItem(index,it) })
                if (index < songs.size - 1) {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        } else {
            //album
            itemsIndexed(songs) { index, item ->
                AlbumSongsItem(
                    order = index + 1,
                    bean = item,
                    onSongItem = { onSongItem(index,it) }
                )
                if (index < songs.size - 1) {
                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
fun FlexibleTopBar(
    title:String,
    isShowTitle: MutableState<Boolean>,
    cover: String,
    albumName: String,
    expendHeight: Dp, //展开时的高度
    collapseHeight: Dp, //折叠时的高度
    offset: MutableState<Float>,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(expendHeight)
            .offset { IntOffset(x = 0, y = offset.value.roundToInt()) }
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = cover,
            contentDescription = "Background",
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(collapseHeight * 2)
        )

        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .offset {
                    //让标题栏始终保持在顶部
                    IntOffset(x = 0, y = -offset.value.roundToInt())
                }
                .fillMaxWidth()
                .height(collapseHeight)
                .statusBarsPadding()
                .padding(start = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = MagicMusicTheme.colors.white,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onBack() }
            )

            AnimatedVisibility(visible = isShowTitle.value) {
                Text(
                    text = albumName,
                    style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Default),
                    color = MagicMusicTheme.colors.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = !isShowTitle.value) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    color = MagicMusicTheme.colors.white,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PlaylistInfo(
    maxHeight: Dp,
    minHeight: Dp,
    cover: String,
    name: String,
    artist: String,
    description: String,
    shareCount: Long,
    commentCount: Long,
    favoriteCount: Long,
    isFollow:Boolean,
    offset: MutableState<Float>,
    onShowDialog: () -> Unit,
    onComment:()->Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = minHeight, start = 20.dp, end = 20.dp, bottom = 20.dp)
            .height(maxHeight - minHeight)
            .offset { IntOffset(x = 0, y = offset.value.roundToInt()) },
    ) {
        val (picRes, nameRes, artistRes, descriptionRes, othersRes) = createRefs()
        AsyncImage(
            model = cover,
            contentDescription = "Playlist Pic",
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .size((LocalConfiguration.current.screenWidthDp / 3).dp)
                .constrainAs(picRes) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle1.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            ),
            color = MagicMusicTheme.colors.white,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(nameRes) {
                start.linkTo(picRes.end, 10.dp)
                end.linkTo(parent.end)
                top.linkTo(parent.top, 10.dp)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = artist,
            style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default),
            color = MagicMusicTheme.colors.white,
            maxLines = 1,
            textAlign = TextAlign.Start,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(artistRes) {
                start.linkTo(picRes.end, 10.dp)
                end.linkTo(parent.end)
                top.linkTo(nameRes.bottom, 5.dp)
                width = Dimension.fillToConstraints
            }
        )

        AnimatedVisibility(
            visible = description.isNotEmpty(),
            modifier = Modifier.constrainAs(descriptionRes) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(picRes.bottom, 10.dp)
                width = Dimension.fillToConstraints
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.caption,
                    color = MagicMusicTheme.colors.white.copy(alpha = 0.8f),
                    maxLines = 1,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Expend",
                    tint = MagicMusicTheme.colors.white.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onShowDialog() }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.constrainAs(othersRes) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(descriptionRes.bottom, 5.dp)
                bottom.linkTo(parent.bottom, 5.dp)
            }
        ) {
            ShareCommentFavorite(
                modifier = Modifier.weight(1f),
                icon = R.drawable.icon_share,
                defaultText = "Share",
                count = shareCount
            ) {}
            ShareCommentFavorite(
                modifier = Modifier.weight(1f),
                icon = R.drawable.icon_comment,
                defaultText = "Comment",
                count = commentCount,
                onClick = onComment
            )
            ShareCommentFavorite(
                modifier = Modifier.weight(1f),
                icon = R.drawable.icon_follow,
                defaultText = "Favorite",
                count = favoriteCount,
                tine = if (isFollow) MagicMusicTheme.colors.selectIcon else MagicMusicTheme.colors.white
            ) {}

        }
    }
}

@Composable
private fun ShareCommentFavorite(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    defaultText: String,
    count: Long,
    tine:Color = MagicMusicTheme.colors.white,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MagicMusicTheme.colors.white.copy(alpha = 0.5f))
            .padding(start = 10.dp, end = 10.dp, top = 5.dp, bottom = 5.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "ShareCommentFavorite",
            tint = tine,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = if (count == 0L) defaultText else transformNum(count),
            style = MaterialTheme.typography.subtitle2.copy(fontFamily = FontFamily.Default),
            color = MagicMusicTheme.colors.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlaylistSongsItem(
    bean: DailySong,
    onSongItem: (Long) -> Unit,
) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clickable { onSongItem(bean.id) }
    ) {
        val (pic, title, num, more) = createRefs()
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
            modifier = Modifier.constrainAs(title) {
                top.linkTo(pic.top, 2.dp)
                start.linkTo(pic.end, 10.dp)
                end.linkTo(more.start)
                width = Dimension.fillToConstraints
            }
        )

        //歌手名称
        Text(
            text = bean.ar[0].name,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(num) {
                bottom.linkTo(pic.bottom, 2.dp)
                start.linkTo(pic.end, 10.dp)
            }
        )

        androidx.compose.material.Icon(
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

@Composable
fun AlbumSongsItem(
    order: Int,
    bean: DailySong,
    onSongItem: (Long) -> Unit,
) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clickable { onSongItem(bean.id) }
    ) {
        val (orderRes, title, num, more) = createRefs()

        Text(
            text = "$order",
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = MagicMusicTheme.colors.textTitle,
            modifier = Modifier.constrainAs(orderRes) {
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
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                start.linkTo(orderRes.end, 10.dp)
                end.linkTo(more.start)
                width = Dimension.fillToConstraints
            }
        )

        //歌手名称
        Text(
            text = bean.ar[0].name,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(num) {
                bottom.linkTo(parent.bottom)
                start.linkTo(orderRes.end, 10.dp)
            }
        )

        androidx.compose.material.Icon(
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

@Composable
private fun PlaylistDialog(
    cover: String,
    name: String,
    tags:List<String>,
    company:String,
    subType:String,
    description: String,
    isPlaylist: Boolean,
    isShowDialog:Boolean,
    onDismiss:()->Unit,
    onSave:()->Unit
){
    if (isShowDialog){
        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(
                //是否可以通过按下后退按钮来关闭对话框。 如果为 true，按下后退按钮将调用 onDismissRequest。
                dismissOnBackPress=true,
                //是否可以通过在对话框边界外单击来关闭对话框。 如果为 true，单击对话框外将调用 onDismissRequest
                dismissOnClickOutside=true,
                //用于在对话框窗口上设置 WindowManager.LayoutParams.FLAG_SECURE 的策略。
                securePolicy= SecureFlagPolicy.Inherit,
                usePlatformDefaultWidth=false //自定义宽度
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ){
                AsyncImage(
                    model = cover,
                    contentDescription = "Background",
                    placeholder = painterResource(id = R.drawable.magicmusic_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur((LocalConfiguration.current.screenWidthDp / 2).dp)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .verticalScroll(state = rememberScrollState()),
                ){
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Close Dialog",
                        tint = MagicMusicTheme.colors.white,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.End)
                            .clickable { onDismiss() }
                    )
                    AsyncImage(
                        model = cover,
                        contentDescription = "Cover",
                        placeholder = painterResource(id = R.drawable.magicmusic_logo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size((LocalConfiguration.current.screenWidthDp / 2).dp)
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(20.dp))
                    )
                    Text(
                        text = name,
                        color = MagicMusicTheme.colors.white,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(modifier = Modifier.fillMaxWidth(), color = MagicMusicTheme.colors.white.copy(alpha = 0.5f))
                    if (isPlaylist){
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Tags:",
                                style = MaterialTheme.typography.caption,
                                color = MagicMusicTheme.colors.white,
                            )
                            if (tags.isEmpty()){
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.caption,
                                    color = MagicMusicTheme.colors.white,
                                )
                            }else{
                                tags.forEach { TagText(tag = it) }
                            }
                        }
                    }else{
                        Text(
                            text = "Company: $company",
                            style = MaterialTheme.typography.caption,
                            color = MagicMusicTheme.colors.white,
                        )
                        Text(
                            text = "Type: $subType",
                            style = MaterialTheme.typography.caption,
                            color = MagicMusicTheme.colors.white,
                        )
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.caption,
                        color = MagicMusicTheme.colors.white,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp,MagicMusicTheme.colors.textContent),
                        onClick = { onSave() },
                        modifier = Modifier
                            .padding(5.dp)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        Text(
                            text = "Save Cover",
                            style = MaterialTheme.typography.subtitle2,
                            color = MagicMusicTheme.colors.white,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagText(tag:String){
    Text(
        text = tag,
        style = MaterialTheme.typography.overline,
        color = MagicMusicTheme.colors.white,
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(10.dp))
            .background(MagicMusicTheme.colors.white.copy(alpha = 0.2f))
            .padding(5.dp)
    )
}