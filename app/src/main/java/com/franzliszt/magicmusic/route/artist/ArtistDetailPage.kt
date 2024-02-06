package com.franzliszt.magicmusic.route.artist

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.artist.Artist
import com.franzliszt.magicmusic.bean.artist.ArtistInfoBean
import com.franzliszt.magicmusic.route.nav.recommend.MusicTabRow
import com.franzliszt.magicmusic.route.nav.recommend.songs.EveryDaySongsItem
import com.franzliszt.magicmusic.route.playlist.AlbumSongsItem
import com.franzliszt.magicmusic.route.searchresult.SearchResultItem
import com.franzliszt.magicmusic.route.searchresult.SearchResultVideoItem
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.customIndicatorOffset
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistDetailPage(
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    onBack:()->Unit,
    onItemMv:(Long)->Unit,
    onArtist: (Long)-> Unit,
    onSongItem:(Long)->Unit,
    onAlbumItem:(Long)->Unit
){
    val value = viewModel.uiStatus.value
    val tabs = remember { ArtistTab.values().map { it.tab } }
    val pageState = androidx.compose.foundation.pager.rememberPagerState{ tabs.size }
    val scrollState = rememberScrollState()
    val coverHeight = (LocalConfiguration.current.screenHeightDp * 0.6).dp
    val nestedScrollConnection = remember {
        object :NestedScrollConnection{
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (available.y > 0) Offset.Zero else Offset(
                    x = 0f,
                    y = -scrollState.dispatchRawDelta(-available.y)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .background(MagicMusicTheme.colors.grayBackground)
            .navigationBarsPadding()
            .padding(bottom = 10.dp)
    ) {
        ArtistInfo(
            isFollow = value.isFollow,
            artist = value.artist,
            height = coverHeight,
            onBack = onBack
        )
        MusicTabRow(pagerState = pageState, tabs = tabs)
        androidx.compose.foundation.pager.HorizontalPager(
            state = pageState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {index->
            when(tabs[index]){
                ArtistTab.Profile.tab->{
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                    ){
                        if (value.artist == null || value.similar.isEmpty()){
                            item { Loading() }
                        }
                        item {
                            Text(
                                text = stringResource(id = R.string.about),
                                color = MagicMusicTheme.colors.textTitle,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        item { ProfileSongInfo(value.artist) }
                        item {
                            Text(
                                text = stringResource(id = R.string.profile),
                                color = MagicMusicTheme.colors.textTitle,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        item { BriefDescription(value.artist?.briefDesc ?: "") }
                        item {
                            Text(
                                text = stringResource(id = R.string.similar),
                                color = MagicMusicTheme.colors.textTitle,
                                style = MaterialTheme.typography.h6
                            )
                        }
                        item { SimilarArtists(value.similar,onArtist) }
                    }
                }
                ArtistTab.Songs.tab->{
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                    ){
                        if (value.songs.isEmpty()){
                            item { Loading() }
                        }
                        itemsIndexed(value.songs){index,song->
                            AlbumSongsItem(
                                bean = song,
                                onSongItem = {
                                    viewModel.playSong(song)
                                    onSongItem(it)
                                },
                                order = index+1
                            )
                        }
                    }
                }
                ArtistTab.Albums.tab->{
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                    ){
                        if (value.albums.isEmpty()){
                            item { Loading() }
                        }
                        items(value.albums){album->
                            SearchResultItem(
                                cover = album.picUrl,
                                nickname = album.name,
                                author = album.artist.name,
                                onClick = { onAlbumItem(album.id) }
                            )
                        }
                    }
                }
                ArtistTab.Mvs.tab->{
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
                    ){
                        if (value.mvs.isEmpty()){
                            item { Loading() }
                        }
                        items(value.mvs){mv->
                            SearchResultVideoItem(
                                cover = mv.imgurl,
                                title = mv.name,
                                author = mv.artistName,
                                playTime = mv.playCount,
                                durationTime = mv.duration,
                                onClick = { onItemMv(mv.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistInfo(
    isFollow: Boolean,
    artist: ArtistInfoBean?,
    height:Dp,
    modifier: Modifier = Modifier,
    onBack:()->Unit
){
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
    ) {
        val (titleRes,bgRes,imgRes,infoRef) = createRefs()
        if (artist != null){
            //背景
            AsyncImage(
                model = artist.cover,
                contentDescription = artist.name,
                placeholder = painterResource(id = R.drawable.magicmusic_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height((LocalConfiguration.current.screenHeightDp * 0.4).dp)
                    .constrainAs(bgRes) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    }
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MagicMusicTheme.colors.background,
                modifier = Modifier
                    .height((LocalConfiguration.current.screenHeightDp * 0.2).dp)
                    .constrainAs(infoRef) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(bgRes.bottom, (-20).dp)
                    }
                    .padding(start = 20.dp, end = 20.dp)
            ) {
                ArtistBriefInfo(
                    isFollow = isFollow,
                    artist = artist
                )
            }

            //头像
            AsyncImage(
                model = artist.avatar,
                contentDescription = artist.name,
                placeholder = painterResource(id = R.drawable.magicmusic_logo),
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .constrainAs(imgRes) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(infoRef.top, (-32).dp)
                    }
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = MagicMusicTheme.colors.background,
                modifier = Modifier
                    .constrainAs(titleRes) {
                        top.linkTo(parent.top,20.dp)
                        start.linkTo(parent.start,20.dp)
                    }
                    .size(24.dp)
                    .clickable { onBack() }
            )
        }
    }
}

@Composable
private fun ArtistBriefInfo(
    isFollow:Boolean,
    artist: ArtistInfoBean
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 40.dp, bottom = 10.dp) //padding
    ) {
        //歌手名称
        Text(
            text = artist.name,
            color =MagicMusicTheme.colors.textTitle,
            style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        //别名
        if (artist.alias.isNotEmpty()){
            Text(
                text = artist.alias.arrayToString(),
                color =MagicMusicTheme.colors.textContent,
                style = MaterialTheme.typography.overline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        //Button
        TextButton(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp,MagicMusicTheme.colors.textContent),
            colors = ButtonDefaults.textButtonColors(
                backgroundColor = if (isFollow) MagicMusicTheme.colors.selectIcon else MagicMusicTheme.colors.background
            ),
            contentPadding = PaddingValues(start = 10.dp,end = 10.dp,top = 5.dp, bottom = 5.dp),
            onClick = {  }
        ) {
            Text(
                text = if (isFollow) "Followed" else "Follow",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.overline
            )
        }
    }
}


@Composable
private fun ProfileSongInfo(artist: ArtistInfoBean?){
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .padding(start = 10.dp, end = 10.dp)
            .background(MagicMusicTheme.colors.background)
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_navigation_recommend),
                contentDescription = "Songs",
                tint = MagicMusicTheme.colors.textContent,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_navigation_finding),
                contentDescription = "Albums",
                tint = MagicMusicTheme.colors.textContent,
                modifier = Modifier.size(24.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.icon_mv),
                contentDescription = "Mv",
                tint = MagicMusicTheme.colors.textContent,
                modifier = Modifier.size(24.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${artist?.musicSize ?: 0}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${artist?.albumSize ?: 0}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${artist?.mvSize ?: 0}",
                color = MagicMusicTheme.colors.textTitle,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun BriefDescription(description:String){
    val isExpend = remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .padding(start = 10.dp, end = 10.dp)
            .background(MagicMusicTheme.colors.background)
            .padding(10.dp)
    ) {
        Text(
            text = description,
            color = MagicMusicTheme.colors.textContent,
            style = MaterialTheme.typography.caption,
            maxLines = if (isExpend.value) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Icon(
            painter = painterResource(id = if (isExpend.value) R.drawable.icon_collapse else R.drawable.icon_expend),
            contentDescription = "Expend Profile",
            tint = MagicMusicTheme.colors.textTitle,
            modifier = Modifier
                .size(24.dp)
                .clickable { isExpend.value = !isExpend.value }
        )
    }
}

@Composable
private fun SimilarArtists(
    artists: List<Artist>,
    onArtist:(Long)->Unit
){
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            items(artists){artist->
                Column(
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .height((LocalConfiguration.current.screenHeightDp / 5).dp)
                        .width((LocalConfiguration.current.screenWidthDp / 4).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MagicMusicTheme.colors.background)
                        .padding(5.dp)
                        .clickable { onArtist(artist.id) }
                ) {
                    AsyncImage(
                        model = artist.picUrl,
                        contentDescription = artist.name,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = artist.name,
                        color = MagicMusicTheme.colors.textTitle,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp,MagicMusicTheme.colors.textContent),
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = if (artist.followed) MagicMusicTheme.colors.selectIcon else MagicMusicTheme.colors.background
                        ),
                        contentPadding = PaddingValues(start = 2.dp,end =2.dp),
                        onClick = {  }
                    ) {
                        Text(
                            text = if (artist.followed) "Followed" else "Follow",
                            color = MagicMusicTheme.colors.textTitle,
                            style = MaterialTheme.typography.overline
                        )
                    }
                }
            }
        }
}

private fun List<String>.arrayToString():String{
    var result = ""
    this.forEachIndexed { index,item->
        result += item
        if (index < this.size-1) result+="、"
    }
    return result
}

private enum class ArtistTab(val tab:String){
    Profile("Profile"),
    Songs("Songs"),
    Albums("Albums"),
    Mvs("Mvs")
}