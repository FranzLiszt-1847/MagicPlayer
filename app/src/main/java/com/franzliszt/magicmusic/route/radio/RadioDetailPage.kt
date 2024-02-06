package com.franzliszt.magicmusic.route.radio

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.radio.program.Program
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.route.playlist.FlexibleTopBar
import com.franzliszt.magicmusic.route.playlist.PlaylistEvent
import com.franzliszt.magicmusic.route.playlist.PlaylistInfo
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun RadioDetailPage(
    viewModel: RadioViewModel = hiltViewModel(),
    onBack:()->Unit,
    onSongItem:(Long)->Unit
){
    val value = viewModel.uiStatus.value
    val maxOffset = with(LocalDensity.current) { (viewModel.maxTopBarHeight.roundToPx().toFloat() - viewModel.minTopBarHeight.roundToPx().toFloat()) }
    val offset = remember { mutableStateOf(0f) }
    val infiniteOffset = remember { mutableStateOf(0f) }
    val isShowTitle = remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    LaunchedEffect( scaffoldState.snackbarHostState ){
        viewModel.eventFlow.collectLatest {
            scope.launch {
                when(it){
                    is RadioStatus.NetworkFailed->{
                        scaffoldState.snackbarHostState.showSnackbar(it.msg)
                    }
                }
            }
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                val delta = available.y
                val newOffset = offset.value + delta
                val newInOffset = infiniteOffset.value + delta
                infiniteOffset.value = newInOffset.coerceIn(-(maxOffset * 1.5f), 0f)
                offset.value = newOffset.coerceIn(-maxOffset, 0f)
                isShowTitle.value = -offset.value.roundToInt() == maxOffset.roundToInt()
                return Offset.Zero
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) {data->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.background)
                .nestedScroll(nestedScrollConnection)
                .navigationBarsPadding()
        ) {
            RadioPrograms(
                programs = value.programs,
                maxTopBarHeight = viewModel.maxTopBarHeight,
                onProgramItem = {
                    viewModel.playProgramItem(it.id)
                    onSongItem(it.id)
                }
            )

            FlexibleTopBar(
                title = "Radio Station",
                isShowTitle = isShowTitle,
                cover = value.detail?.picUrl ?: "",
                albumName = value.detail?.name ?: "Unknown",
                expendHeight = viewModel.maxTopBarHeight,
                collapseHeight = viewModel.minTopBarHeight,
                offset = offset,
                onBack = onBack
            )

            PlaylistInfo(
                maxHeight = viewModel.maxTopBarHeight,
                minHeight = viewModel.minTopBarHeight,
                cover = value.detail?.picUrl ?: "",
                name = value.detail?.name ?: "Unknown",
                artist = value.detail?.dj?.nickname ?: "Unknown",
                description = value.detail?.dj?.signature ?: "Unknown",
                shareCount = value.detail?.shareCount ?: 0,
                commentCount = value.detail?.commentCount ?: 0,
                favoriteCount = value.detail?.subCount ?: 0,
                isFollow = value.detail?.subed ?: false,
                offset = infiniteOffset,
                onShowDialog = {  },
                onComment = {}
            )
        }
    }
}

@Composable
private fun RadioPrograms(
    programs:List<Program>,
    maxTopBarHeight: Dp,
    onProgramItem: (Program) -> Unit
){
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .clip(RoundedCornerShape(topEnd = 40.dp, topStart = 40.dp))
            .background(MagicMusicTheme.colors.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = maxTopBarHeight + (10).dp)
    ){
        if (programs.isEmpty()){
            item { Loading() }
        }
        items(programs){
            ProgramItem(
                bean = it,
                onProgramItem = onProgramItem
            )
        }
    }
}

@Composable
private fun ProgramItem(
    bean: Program,
    onProgramItem: (Program) -> Unit,
) {
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .clickable { onProgramItem(bean) }
    ) {
        val (pic, title, num, more) = createRefs()
        AsyncImage(
            model = bean.coverUrl,
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
            text = bean.dj.nickname,
            style = MaterialTheme.typography.caption,
            color = MagicMusicTheme.colors.textContent,
            modifier = Modifier.constrainAs(num) {
                bottom.linkTo(pic.bottom, 2.dp)
                start.linkTo(pic.end, 10.dp)
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