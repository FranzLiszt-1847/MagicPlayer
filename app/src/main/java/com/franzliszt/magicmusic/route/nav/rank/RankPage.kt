package com.franzliszt.magicmusic.route.nav.rank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.bean.rank.Rankbean
import com.franzliszt.magicmusic.route.nav.recommend.artist.items
import com.franzliszt.magicmusic.route.nav.recommend.playlist.RecommendPlaylistStickyHeader
import com.franzliszt.magicmusic.tool.Loading
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme

@Composable
fun RankPage(
    viewModel: RankViewModel = hiltViewModel(),
    onRank:(Long)->Unit
){
    val value = viewModel.uiStatus.value
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MagicMusicTheme.colors.background)
            .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
    ){
        if (value.official.isEmpty() || value.global.isEmpty()){
            item(span = {GridItemSpan(this.maxLineSpan)}) { Loading() }
        }
        item(span = {GridItemSpan(this.maxLineSpan)}){
            Text(
                text = stringResource(id = R.string.rank),
                style = MaterialTheme.typography.h5,
                color = MagicMusicTheme.colors.textTitle
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        item(span = {GridItemSpan(this.maxLineSpan)}){
            OfficialRankList(value.official,onRank)
            Spacer(modifier = Modifier.height(20.dp))
        }
        item(span = {GridItemSpan(this.maxLineSpan)}){
            RecommendPlaylistStickyHeader(header = stringResource(id = R.string.global_chart))
            Spacer(modifier = Modifier.height(10.dp))
        }
        items(value.global.size){
            GlobalListItem(bean = value.global[it],onRank = onRank)
        }
    }
}

@Composable
private fun OfficialRankList(
    charts:List<Rankbean>,
    onRank:(Long)->Unit
){
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        items(charts.size){
            OfficialRankListItem(bean = charts[it],onRank = onRank)
            if (it < charts.size-1)
                Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
private fun OfficialRankListItem(
    bean:Rankbean,
    maxHeight: Int = LocalConfiguration.current.screenHeightDp,
    maxWidth:Int = LocalConfiguration.current.screenWidthDp,
    onRank:(Long)->Unit
){
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .width((maxWidth / 1.2).dp)
            .height((maxHeight / 5).dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        MagicMusicTheme.colors.backgroundBottom,
                        MagicMusicTheme.colors.backgroundEnd
                    )
                )
            )
            .clickable { onRank(bean.id) }
    ) {
        Row( modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bean.name,
                    style = MaterialTheme.typography.subtitle1,
                    color = MagicMusicTheme.colors.textTitle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                AsyncImage(
                    model = bean.coverImgUrl,
                    contentDescription = bean.name,
                    placeholder = painterResource(id = R.drawable.magicmusic_logo),
                    contentScale = ContentScale.Inside,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .weight(1f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = bean.updateFrequency,
                    style = MaterialTheme.typography.subtitle1,
                    color = MagicMusicTheme.colors.background,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.weight(1f)
                ){
                    items(bean.tracks.size){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Text(
                                text = "${it+1}",
                                style = MaterialTheme.typography.body2,
                                fontWeight = FontWeight.Bold,
                                color = MagicMusicTheme.colors.textTitle
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = bean.tracks[it].first,
                                style = MaterialTheme.typography.body2,
                                color = MagicMusicTheme.colors.textTitle,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                            )
                            Text(
                                text = " — ${bean.tracks[it].second}",
                                style = MaterialTheme.typography.caption,
                                color = MagicMusicTheme.colors.background,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalListItem(
    bean: Rankbean,
    maxHeight: Int = LocalConfiguration.current.screenHeightDp,
    onRank:(Long)->Unit
){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((maxHeight / 6).dp)
            .background(MagicMusicTheme.colors.background)
            .clickable { onRank(bean.id) }
    ){
        AsyncImage(
            model = bean.coverImgUrl,
            contentDescription = bean.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.magicmusic_logo),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        )

        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = bean.name,
            tint = MagicMusicTheme.colors.background,
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 5.dp, bottom = 5.dp)
        )
    }
}