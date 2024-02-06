package com.franzliszt.magicmusic.tool

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material.Surface
import androidx.compose.material.TabPosition
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.banner.BannerBean
import com.franzliszt.magicmusic.bean.comment.BaseCommentBean
import com.franzliszt.magicmusic.bean.comment.CommentBean
import com.franzliszt.magicmusic.route.drawer.user.transformData
import com.franzliszt.magicmusic.route.nav.recommend.playlist.transformNum
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.route.searchresult.transformTime
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun TopTitleBar(
    modifier: Modifier =Modifier,
    titleBarBg:Color =  Color.Transparent, //标题栏背景颜色
    iconTint:Color = MagicMusicTheme.colors.textTitle,
    title:String, //标题栏中间的标题
    onBack:()->Unit
){
    ConstraintLayout(modifier = modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .background(color = titleBarBg),
       ) {
       val (backRes,titleRes) = createRefs()

        IconButton(
            onClick = { onBack() },
            modifier = Modifier.constrainAs(backRes){
                top.linkTo(parent.top)
                bottom.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            color = iconTint,
            modifier = Modifier
                .constrainAs(titleRes){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MagicMusicBanner(
    modifier: Modifier = Modifier,
    items:List<BannerBean>,
    autoLoop:Boolean = true,
    bannerHeight:Dp = (LocalConfiguration.current.screenHeightDp.dp/6),
    loopTimes:Long = 3000L,
    selectSize:Dp = 8.dp,
    unSelectSize:Dp = 4.dp,
    inactiveColor:Color = MagicMusicTheme.colors.defaultIcon,//指示器未选中颜色
    activeColor:Color = MagicMusicTheme.colors.highlightColor,//指示器选中颜色
    onItemClick:(BannerBean)->Unit
){
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    var isAutoLoop by remember { mutableStateOf(autoLoop) }

    //当发生手动滑动事件时，取消自动播放
    LaunchedEffect(pagerState.interactionSource){
        pagerState.interactionSource.interactions.collect{
            isAutoLoop = when(it){
                is DragInteraction.Start-> false
                else-> true
            }
        }
    }

    //自动播放
    if (items.isNotEmpty()){
        LaunchedEffect(pagerState.currentPage,isAutoLoop){
            if (isAutoLoop){
                delay(loopTimes)
                val next = (pagerState.currentPage + 1) % items.size
                scope.launch { pagerState.animateScrollToPage(next) }
            }
        }
    }

    HorizontalPager(
        count = items.size,
        state = pagerState,
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(bannerHeight)
        ) {
            repeat(items.size){
                AsyncImage(
                    model = items[pagerState.currentPage].pic,
                    contentDescription = items[pagerState.currentPage].typeTitle,
                    placeholder = painterResource(id = R.drawable.magicmusic_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onItemClick(items[pagerState.currentPage])
                        }
                )
            }
            HorizontalPagerIndicator(
                pageCount = items.size,
                pagerState = pagerState,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                indicatorWidth = if (pagerState.currentPage == it)selectSize else unSelectSize,
                indicatorHeight = if (pagerState.currentPage == it)selectSize else unSelectSize,
                indicatorShape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
            )
        }
    }
}

/**
 * 重写TabRow的pagerTabIndicatorOffset
 * 让TabRow 的indicator宽度可以进行自定义*/
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.customIndicatorOffset(
    pagerState: androidx.compose.foundation.pager.PagerState,
    tabPositions: List<TabPosition>,
    width: Dp
): Modifier = composed {
    if (pagerState.pageCount == 0) return@composed this

    val targetIndicatorOffset: Dp
    val indicatorWidth: Dp

    val currentTab = tabPositions[minOf(tabPositions.lastIndex, pagerState.currentPage)]
    val targetPage = pagerState.targetPage
    val targetTab = tabPositions.getOrNull(targetPage)

    if (targetTab != null) {
        val targetDistance = (targetPage - pagerState.currentPage).absoluteValue
        val fraction = (pagerState.currentPageOffsetFraction / max(targetDistance, 1)).absoluteValue

        targetIndicatorOffset = lerp(currentTab.left, targetTab.left, fraction)
        indicatorWidth = lerp(currentTab.width, targetTab.width, fraction).value.absoluteValue.dp
    } else {
        targetIndicatorOffset = currentTab.left
        indicatorWidth = currentTab.width
    }

    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .padding(horizontal = (indicatorWidth - width) / 2)
        .offset(x = targetIndicatorOffset)
        .width(width)
}

/**
 * 顶部圆圈加载*/
@Composable
fun Loading(){
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
    ){
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp),
            color = MagicMusicTheme.colors.highlightColor
        )
    }
}

/**
 * 加载失败*/
@Composable
fun LoadingFailed(
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    content:String = "Loading failed,please retry!",
    onClick:()->Unit
){
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_loading_failed),
                contentDescription = "Loading Failed",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size((maxHeight/5).dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.caption,
                color = MagicMusicTheme.colors.textContent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    value: String,
    hint: String,
    focusRequester:FocusRequester = FocusRequester(),
    focusManager:FocusManager = LocalFocusManager.current,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit = {}
) {
    Surface(
        border = BorderStroke(width = 1.dp, color = MagicMusicTheme.colors.searchBar),
        shape = RoundedCornerShape(10.dp),
        color = MagicMusicTheme.colors.background,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        CustomTextField(
            value = value,
            valueColor = MagicMusicTheme.colors.textTitle,
            hintColor = MagicMusicTheme.colors.textContent,
            onValueChange = onValueChange,
            placeholderText = hint,
            textStyle = MaterialTheme.typography.caption,
            maxLines = 1,
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                onSearch()
            }),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "back",
                    tint = MagicMusicTheme.colors.textTitle,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBack() }
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MagicMusicTheme.colors.unselectIcon,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            focusManager.clearFocus()
                            onSearch()
                        }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        keyboardController?.hide()
                    }
                }
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    valueColor:Color,
    hintColor: Color,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholderText: String = "",
    onTextLayout: (TextLayoutResult) -> Unit = {},
    cursorBrush: Brush = SolidColor(Color.Black),
) {
    BasicTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        readOnly = readOnly,
        interactionSource = interactionSource,
        textStyle = textStyle.copy(color = valueColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        onTextLayout = onTextLayout,
        cursorBrush = cursorBrush,
        decorationBox = { innerTextField ->
            Row(
                modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) leadingIcon()
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty())
                        Text(
                            text = placeholderText,
                            style = textStyle.copy(color = hintColor),
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    innerTextField()
                }
                if (trailingIcon != null) trailingIcon()
            }
        }
    )
}

@Composable
fun CustomAlertDialog(
    visibility: Boolean,
    title: String,
    content: String,
    confirmText: String,
    cancelText: String,
    onConfirm:()->Unit,
    onCancel:()->Unit,
    onDismiss:()->Unit
){
    if (visibility){
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    color = MagicMusicTheme.colors.highlightColor,
                    style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) },
            text = {
                Text(
                    text = content,
                    color = MagicMusicTheme.colors.textTitle,
                    style = MaterialTheme.typography.caption
                )
            },
            shape = RoundedCornerShape(10.dp),
            backgroundColor = MagicMusicTheme.colors.background,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = confirmText,
                        color = MagicMusicTheme.colors.highlightColor,
                        style = MaterialTheme.typography.button
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(
                        text = cancelText,
                        color = MagicMusicTheme.colors.textContent,
                        style = MaterialTheme.typography.button
                    )
                }
            },
            properties = DialogProperties(
                //是否可以通过按下后退按钮来关闭对话框。 如果为 true，按下后退按钮将调用 onDismissRequest。
                dismissOnBackPress=true,
                //是否可以通过在对话框边界外单击来关闭对话框。 如果为 true，单击对话框外将调用 onDismissRequest
                dismissOnClickOutside=true,
                //用于在对话框窗口上设置 WindowManager.LayoutParams.FLAG_SECURE 的策略。
                securePolicy= SecureFlagPolicy.Inherit,
                //对话框内容的宽度是否应限制为平台默认值，小于屏幕宽度。
                usePlatformDefaultWidth=true
            )
        )
    }
}

/**
 * 评论BottomSheet*/
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CommentBottomSheet(
    state:LazyListState,
    text: String,
    status:NetworkStatus,
    commentCount:Long,
    cover:String,
    name:String,
    artist:String,
    comments:List<CommentBean>,
    coverHeight: Dp = 64.dp,
    picHeight:Dp = 32.dp,
    onAgreeComment: (Long, Int) -> Unit,
    onFloorComment: (Long,Int) -> Unit,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((LocalConfiguration.current.screenHeightDp * 0.9f).dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.background)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Divider(
                color = MagicMusicTheme.colors.textContent,
                thickness = DividerDefaults.Thickness,
                modifier = Modifier
                    .width(40.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = "Comments($commentCount)",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold
                ),
                color = MagicMusicTheme.colors.textTitle,
                textAlign = TextAlign.Center
            )
            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = coverHeight+picHeight)
            ) {

                item {
                    PlaylistBriefInfo(
                        cover = cover,
                        name = name,
                        artist = artist,
                        coverHeight = coverHeight
                    )
                }

                stickyHeader {
                    Text(
                        text = "Comment section",
                        style = MaterialTheme.typography.subtitle2.copy(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MagicMusicTheme.colors.textTitle,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (status) {
                    is NetworkStatus.Waiting -> {
                        item { Loading() }
                    }

                    is NetworkStatus.Failed -> {
                        item { LoadingFailed(content = status.error) {} }
                    }

                    is NetworkStatus.Successful -> {
                        //加载成功，但是没有评论
                        if (comments.isEmpty()) {
                            item { LoadingFailed(content = "Not comments!") {} }
                        }
                    }
                }

                //评论
                itemsIndexed(comments) { index, item ->
                    CommentItem(
                        picHeight = picHeight,
                        comment = item,
                        onAgreeComment = {
                            onAgreeComment(it, index)
                        },
                        onFloorComment = {
                            onFloorComment(it, index)
                        }
                    )
                    if (index < comments.size - 1) {
                        Divider(
                            color = MagicMusicTheme.colors.textContent.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = picHeight, top = 10.dp)
                        )
                    }
                }
            }
        }
        CommentBar(
            text = text,
            onValueChange = onValueChange,
            onSend = {
                onSend()
                focusManager.clearFocus()
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            keyboardActions = KeyboardActions (onSend = {
                onSend()
                focusManager.clearFocus()
            }),
            textModifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        keyboardController?.hide()
                    }
                }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun FloorCommentBottomSheet(
    state:LazyListState,
    text: String,
    status:NetworkStatus,
    commentCount:Long,
    coverHeight: Dp = 64.dp,
    picHeight:Dp = 32.dp,
    ownComment:CommentBean?,
    comments:List<CommentBean>,
    onAgreeComment: (Long, Int) -> Unit,
    onValueChange: (String) -> Unit,
    onSend: (Long) -> Unit
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((LocalConfiguration.current.screenHeightDp * 0.9f).dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.background)
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Divider(
                color = MagicMusicTheme.colors.textContent,
                thickness = DividerDefaults.Thickness,
                modifier = Modifier
                    .width(40.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            Text(
                text = "Replies($commentCount)",
                style = MaterialTheme.typography.subtitle1.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold
                ),
                color = MagicMusicTheme.colors.textTitle,
                textAlign = TextAlign.Center
            )
            LazyColumn(
                state = state,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = coverHeight+picHeight)
            ) {
                item {
                    if (ownComment != null) {
                        OwnCommentItem(
                            comment = ownComment,
                            picHeight = picHeight
                        )
                        Divider(
                            color = MagicMusicTheme.colors.textContent.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .padding(top = 10.dp)
                        )
                    }
                }

                stickyHeader {
                    Text(
                        text = "All replies",
                        style = MaterialTheme.typography.subtitle2.copy(
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MagicMusicTheme.colors.textTitle,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (status) {
                    is NetworkStatus.Waiting -> {
                        item { Loading() }
                    }

                    is NetworkStatus.Failed -> {
                        item { LoadingFailed(content = status.error) {} }
                    }

                    is NetworkStatus.Successful -> {
                        //加载成功，但是没有评论
                        if (comments.isEmpty()) {
                            item { LoadingFailed(content = "Not comments!") {} }
                        }
                    }
                }

                //评论
                itemsIndexed(comments) { index, item ->
                    CommentItem(
                        showExpend = false,
                        picHeight = picHeight,
                        comment = item,
                        onAgreeComment = {
                            onAgreeComment(it, index)
                        },
                        onFloorComment = {}
                    )
                    if (index < comments.size - 1) {
                        Divider(
                            color = MagicMusicTheme.colors.textContent.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = picHeight, top = 10.dp)
                        )
                    }
                }
            }
        }
        CommentBar(
            text = text,
            onValueChange = onValueChange,
            onSend = {
                onSend(ownComment?.commentId ?: 0L)
                focusManager.clearFocus()
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            keyboardActions = KeyboardActions (onSend = {
                onSend(ownComment?.commentId ?: 0L)
                focusManager.clearFocus()
            }),
            textModifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (!it.isFocused) {
                        keyboardController?.hide()
                    }
                }
        )
    }
}

/**
 * 评论的歌单信息*/
@Composable
private fun PlaylistBriefInfo(
    cover:String,
    name:String,
    artist:String,
    coverHeight:Dp
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(coverHeight)
    ) {
        AsyncImage(
            model = cover,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .size(coverHeight)
                .clip(RoundedCornerShape(20.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .height(coverHeight)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
                color = MagicMusicTheme.colors.textTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = artist,
                style = MaterialTheme.typography.caption,
                color = MagicMusicTheme.colors.textContent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "More",
            tint = MagicMusicTheme.colors.defaultIcon,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun CommentItem(
    showExpend:Boolean = true,
    picHeight:Dp,
    comment:CommentBean,
    onFloorComment:(Long)->Unit,
    onAgreeComment:(Long)->Unit
){
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (picRes,nameRes,dateRes,agreeRes,commentRes,floorRes) = createRefs()
        //评论者图像
        AsyncImage(
            model = comment.user.avatarUrl,
            contentDescription = comment.user.nickname,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .size(picHeight)
                .clip(CircleShape)
                .constrainAs(picRes) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )
        //评论者名称
        Text(
            text = comment.user.nickname,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textTitle.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.constrainAs(nameRes){
                top.linkTo(picRes.top)
                start.linkTo(picRes.end,5.dp)
                end.linkTo(agreeRes.start)
                width = Dimension.fillToConstraints
            }
        )
        //评论日期
        Text(
            text = comment.timeStr ?: transformData(comment.time,"yyyy-MM-dd"),
            style = MaterialTheme.typography.overline.copy(fontFamily = FontFamily.Default),
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(dateRes){
                bottom.linkTo(picRes.bottom)
                start.linkTo(picRes.end,5.dp)
            }
        )

        //点赞icon及点赞人数
        IconButton(
            onClick = { onAgreeComment(comment.commentId) },
            modifier = Modifier.constrainAs(agreeRes){
                top.linkTo(nameRes.top)
                bottom.linkTo(dateRes.bottom)
                end.linkTo(parent.end)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if(comment.likedCount == null || comment.likedCount == 0) "" else "${comment.likedCount}",
                    style = MaterialTheme.typography.caption,
                    color = if (comment.liked) MagicMusicTheme.colors.selectIcon else MagicMusicTheme.colors.unselectIcon,
                )
                Icon(
                    painter = painterResource(id = R.drawable.icon_agree_comment),
                    contentDescription = "Agree",
                    tint = if (comment.liked) MagicMusicTheme.colors.selectIcon else MagicMusicTheme.colors.unselectIcon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        //评论内容
        Text(
            text = comment.content,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textTitle,
            modifier = Modifier.constrainAs(commentRes){
                top.linkTo(picRes.bottom,5.dp)
                start.linkTo(picRes.end,5.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        AnimatedVisibility(
            visible = showExpend,
            modifier = Modifier
                .constrainAs(floorRes) {
                    top.linkTo(commentRes.bottom, 5.dp)
                    start.linkTo(picRes.end, 5.dp)
                }
        ) {
            //楼层回复
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onFloorComment(comment.commentId) }
            ) {
                Text(
                    text = "expend replies",
                    style = MaterialTheme.typography.caption,
                    color = MagicMusicTheme.colors.selectIcon,
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Expend Reply",
                    tint = MagicMusicTheme.colors.selectIcon,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

    }
}

@Composable
private fun OwnCommentItem(
    picHeight:Dp,
    comment:CommentBean
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (picRes, nameRes, dateRes, commentRes) = createRefs()
        //评论者图像
        AsyncImage(
            model = comment.user.avatarUrl,
            contentDescription = comment.user.nickname,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .size(picHeight)
                .clip(CircleShape)
                .constrainAs(picRes) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )
        //评论者名称
        Text(
            text = comment.user.nickname,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textTitle.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            modifier = Modifier.constrainAs(nameRes) {
                top.linkTo(picRes.top)
                start.linkTo(picRes.end, 5.dp)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
        //评论日期
        Text(
            text =comment.timeStr ?: transformData(comment.time,"yyyy-MM-dd"),
            style = MaterialTheme.typography.overline.copy(fontFamily = FontFamily.Default),
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(dateRes) {
                bottom.linkTo(picRes.bottom)
                start.linkTo(picRes.end, 5.dp)
            }
        )

        //评论内容
        Text(
            text = comment.content,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textTitle,
            modifier = Modifier.constrainAs(commentRes) {
                top.linkTo(picRes.bottom, 5.dp)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )
    }
}

@Composable
private fun CommentBar(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    text:String,
    keyboardActions: KeyboardActions,
    onValueChange: (String) -> Unit,
    onSend:()->Unit
){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 20.dp, end = 20.dp)
            .navigationBarsPadding()
            .background(MagicMusicTheme.colors.background)
            .padding(top = 2.dp, bottom = 2.dp)
            .imePadding()
    ) {
        CustomTextField(
            value = text,
            valueColor = MagicMusicTheme.colors.textTitle,
            hintColor = MagicMusicTheme.colors.textContent,
            placeholderText = "Your heartwarming comments may console “another you“",
            maxLines = 1,
            textStyle = MaterialTheme.typography.caption,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Send
            ),
            keyboardActions = keyboardActions,
            modifier = textModifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MagicMusicTheme.colors.grayBackground)
                .padding(5.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        TextButton(onClick = onSend) {
            Text(
                text = "Send",
                style = MaterialTheme.typography.caption,
                color = MagicMusicTheme.colors.highlightColor
            )
        }
    }
}