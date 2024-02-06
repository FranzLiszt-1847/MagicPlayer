package com.franzliszt.magicmusic.route.nav.mine

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.playlist.Playlist
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.tool.LoadingFailed
import com.franzliszt.magicmusic.tool.MagicMusicBanner
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinePage(
    viewModel: MineViewModel = hiltViewModel(),
    onPlaylist: (Long) -> Unit
){
    val value = viewModel.uiStatus.value
    val scrollState = rememberScrollState()
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
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(color = MagicMusicTheme.colors.background)
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
        ) {
            Text(
                text = stringResource(id = R.string.songs),
                style = MaterialTheme.typography.h5,
                color = MagicMusicTheme.colors.textTitle
            )
            Spacer(modifier = Modifier.height(10.dp))
            MagicMusicBanner(items = value.banners){

            }
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp)
            ){
                when(value.playlistState){
                    is NetworkStatus.Waiting->{item { Loading() }}
                    is NetworkStatus.Failed->{ item { LoadingFailed(content = value.playlistState.error) {} }}
                    is NetworkStatus.Successful->{
                        if (value.mapPlaylist.isEmpty()){
                            item { LoadingFailed(content = stringResource(id = R.string.not_playlist)) {} }
                        }
                    }
                }
                value.mapPlaylist.forEach {
                    stickyHeader {
                        PlaylistStickyHeader(it)
                    }
                    when(it.key){
                        Constants.Preference->item {
                            value.preferBean?.let {
                                PlaylistItem(value.preferBean!!, onPlaylist = onPlaylist)
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                        Constants.Create-> items(value.creates.size){index->
                            if (value.creates.isNotEmpty()){
                                PlaylistItem(value.creates[index],onPlaylist = onPlaylist)
                                if (index < value.creates.size - 1) Spacer(modifier = Modifier.height(10.dp))
                                else Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                        Constants.Favorite-> items(value.favorites.size){index->
                            if (value.favorites.isNotEmpty()){
                                PlaylistItem(value.favorites[index],onPlaylist = onPlaylist)
                                if (index < value.favorites.size - 1) Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistStickyHeader(pair:Map.Entry<String, Boolean>){
    if (pair.value){
        Text(
            text = pair.key,
            style = MaterialTheme.typography.h6,
            color = MagicMusicTheme.colors.textTitle
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun PlaylistItem(
    bean:Playlist,
    onPlaylist:(Long)->Unit
){
    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .background(MagicMusicTheme.colors.background)
        .clickable { onPlaylist(bean.id) }
    ) {
        val (pic,title,num,more) = createRefs()
        AsyncImage(
            model = bean.coverImgUrl,
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
