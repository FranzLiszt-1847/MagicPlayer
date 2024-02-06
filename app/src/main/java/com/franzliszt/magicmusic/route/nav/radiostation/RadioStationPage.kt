package com.franzliszt.magicmusic.route.nav.radiostation

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.bean.radio.RecommendRadioBean
import com.franzliszt.magicmusic.bean.radio.program.NewHotRadioBean
import com.franzliszt.magicmusic.bean.radio.program.ProgramRankBean
import com.franzliszt.magicmusic.route.nav.recommend.MusicTabRow
import com.franzliszt.magicmusic.route.nav.recommend.playlist.RecommendPlaylistStickyHeader
import com.franzliszt.magicmusic.route.nav.recommend.songs.RecommendSongsPage
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.tool.MagicMusicBanner
import com.franzliszt.magicmusic.tool.customIndicatorOffset
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn( ExperimentalFoundationApi::class)
@Composable
fun RadioStationPage(
    viewModel: RadioStationViewModel = hiltViewModel(),
    onRadio:(Long)->Unit,
    onSongItem:(Long)->Unit
){
    val titles = remember { ProgramRankingHeader.values() }
    val programStatus = viewModel.getProgramRanking().collectAsLazyPagingItems()
    val newRadioStatus = viewModel.getNewProgramRanking().collectAsLazyPagingItems()
    val hotRadioStatus = viewModel.getHotProgramRanking().collectAsLazyPagingItems()

    val scaffoldState = rememberScaffoldState()
    val scrollState = rememberScrollState()
    val nestedScrollConnection = remember {
        object :NestedScrollConnection{
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.y > 0)
                    Offset.Zero
                else
                    Offset(0f, -scrollState.dispatchRawDelta(-available.y))
            }
        }
    }

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
        Surface(
            color = MagicMusicTheme.colors.background,
            modifier = Modifier.fillMaxSize()

        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    text = stringResource(id = R.string.radio_station),
                    style = MaterialTheme.typography.h5,
                    color = MagicMusicTheme.colors.textTitle
                )
                MagicMusicBanner(items = viewModel.banners){}
                Spacer(modifier = Modifier.height(10.dp))
                titles.forEach { title->
                    RecommendPlaylistStickyHeader(header = stringResource(id = title.header))
                    when(title.header){
                        ProgramRankingHeader.Recommend.header->{
                            RadioStationList(viewModel.recommends,onRadio)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        ProgramRankingHeader.Hot.header->{
                            RadioStationList(viewModel.hots,onRadio)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        ProgramRankingHeader.TopList.header->{
                            ProgramRankPager(
                                programStatus = programStatus,
                                newRadioStatus = newRadioStatus,
                                hotRadioStatus = hotRadioStatus,
                                nestedScrollConnection = nestedScrollConnection,
                                onRadio = onRadio,
                                onItemRadio = {
                                    viewModel.playProgram(it)
                                    onSongItem(it.program.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioStationList(
    recommends: List<RecommendRadioBean>,
    onRadio:(Long)->Unit
){
    LazyRow(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ){
        items(recommends.size){
            RadioStationListItem(
               bean = recommends[it],
                onRadio = onRadio
            )
            if (it < recommends.size-1)
                Spacer(modifier = Modifier.width(15.dp))
        }
    }
}

@Composable
private fun RadioStationListItem(
    bean: RecommendRadioBean,
    maxHeight:Int = LocalConfiguration.current.screenHeightDp,
    maxWidth:Int = LocalConfiguration.current.screenWidthDp,
    onRadio:(Long)->Unit
){
    Column (
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .height((maxHeight / 5).dp)
            .width((maxWidth / 4).dp)
            .clickable { onRadio(bean.id) }
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
            text = "${bean.dj.nickname} | ${bean.name}",
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
private fun ProgramRankPager(
    programStatus: LazyPagingItems<ProgramRankBean>,
    newRadioStatus: LazyPagingItems<NewHotRadioBean>,
    hotRadioStatus: LazyPagingItems<NewHotRadioBean>,
    nestedScrollConnection:NestedScrollConnection,
    tabs:List<String> = remember { ProgramRankingTab.values().map { it.tab } },
    pageState:androidx.compose.foundation.pager.PagerState = androidx.compose.foundation.pager.rememberPagerState{tabs.size},
    onRadio:(Long)->Unit,
    onItemRadio: (ProgramRankBean) -> Unit
){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        MusicTabRow(pagerState = pageState, tabs = tabs)
        androidx.compose.foundation.pager.HorizontalPager(
            state = pageState
        ) {
            when(tabs[it]){
                ProgramRankingTab.Program.tab->{
                    ProgramRankList(programStatus, onItemRadio = onItemRadio)
                }
                ProgramRankingTab.NewRadio.tab->{
                    NewHotRankList(newRadioStatus,onRadio)
                }
                ProgramRankingTab.PopularRadio.tab->{
                    NewHotRankList(hotRadioStatus,onRadio)
                }
            }
        }
    }
}

@Composable
private fun ProgramRankList(
    programStatus: LazyPagingItems<ProgramRankBean>,
    onItemRadio:(ProgramRankBean)->Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.Center
    ){
        when(programStatus.loadState.refresh){
            is LoadState.Loading->{
                item { Loading() }
            }

            is LoadState.Error->{
                item { LoadingFailed {programStatus.retry()} }
            }
            else->{}
        }
        items(items = programStatus){ item->
            item?.let { bean->
                ProgramRankListItem(
                    curRank = bean.rank,
                    lastRank = bean.lastRank,
                    cover = bean.program.blurCoverUrl,
                    name = bean.program.name,
                    author = bean.program.dj.nickname,
                    onRadio = {onItemRadio(bean)}
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        when(programStatus.loadState.append){
            is LoadState.Loading-> {
                item { Loading() }
            }
            is LoadState.Error-> {
                item { LoadingFailed {programStatus.retry()} }
            }
            else ->{}
        }
    }
}

@Composable
private fun NewHotRankList(
    status: LazyPagingItems<NewHotRadioBean>,
    onRadio:(Long)->Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            .background(MagicMusicTheme.colors.background),
        verticalArrangement = Arrangement.Center
    ){
        when(status.loadState.refresh){
            is LoadState.Loading->{ item { Loading() } }
            is LoadState.Error->{ item { LoadingFailed {} } }
            else->{}
        }
        items(items = status){ item->
            item?.let { bean->
                ProgramRankListItem(
                    curRank = bean.rank,
                    lastRank = bean.lastRank,
                    cover = bean.picUrl,
                    name = bean.name,
                    author = bean.creatorName,
                    onRadio = { onRadio(bean.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        when(status.loadState.append){
            is LoadState.Loading-> { item { Loading() } }
            is LoadState.Error-> { item { LoadingFailed {} } }
            else->{}
        }
    }
}

@Composable
private fun ProgramRankListItem(
    curRank:Int,
    lastRank:Int,
    cover:String,
    name:String,
    author:String,
    onRadio:()->Unit
){
    val color:Color
    val icon: Int
     if(curRank == lastRank){
        //排名不变
         color = MagicMusicTheme.colors.stableRank
         icon = R.drawable.icon_rank_stable
    }else if (curRank-lastRank > 0){
        //排名下降
         color = MagicMusicTheme.colors.downRank
         icon = R.drawable.icon_rank_down
    }else{
       //排名上升
         color = MagicMusicTheme.colors.upRank
         icon = R.drawable.icon_rank_up
    }
        ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onRadio() }
        ) {
            val (cur,last,pic,title,num,more) = createRefs()
            Text(
                text = "$curRank",
                color = MagicMusicTheme.colors.highlightColor,
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.constrainAs(cur){
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(last.start)
                    end.linkTo(last.end)
                }
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.constrainAs(last){
                    top.linkTo(cur.bottom)
                    start.linkTo(parent.start)
                }
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "rank",
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )

                Text(
                    text = "${abs(curRank-lastRank)}",
                    style = MaterialTheme.typography.overline.copy(fontFamily = FontFamily.Default),
                    color = color
                )
            }

            AsyncImage(
                model = cover,
                contentDescription = name,
                placeholder = painterResource(id = R.drawable.magicmusic_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .constrainAs(pic) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(last.end, 10.dp)
                    }
            )

            Text(
                text = name,
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

            Text(
                text = author,
                style = MaterialTheme.typography.caption,
                color = MagicMusicTheme.colors.textContent,
                maxLines = 1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
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

private enum class ProgramRankingHeader(@StringRes val header:Int){
    Recommend(R.string.recommend),
    Hot(R.string.hot),
    TopList(R.string.rank),
}

private enum class ProgramRankingTab(val tab:String){
    Program("Program"),
    NewRadio("New Radio"),
    PopularRadio("Hot Radio"),
}