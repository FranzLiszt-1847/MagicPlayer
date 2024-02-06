package com.franzliszt.magicmusic.route.drawer.about

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.route.drawer.recent.TopTitleBar
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun AboutPage(
    viewModel: AboutViewModel = AboutViewModel(),
    onBack:()->Unit
){
    val tags = viewModel.tags
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(key1 = scaffoldState.snackbarHostState){
        viewModel.eventFlow.collectLatest {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {hostState->
            SnackbarHost(hostState = hostState, modifier = Modifier.navigationBarsPadding()){data->
                Snackbar(snackbarData = data)
            }
        }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(MagicMusicTheme.colors.grayBackground)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp)
        ){
            item {
                TopTitleBar("About",onBack)
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.magicmusic_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(128.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            items(tags.size){ index->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MagicMusicTheme.colors.background)
                        .padding(20.dp)
                        .clickable { viewModel.onEvent(tags[index].key) }
                ) {
                    Text(
                        text = tags[index].key,
                        color = MagicMusicTheme.colors.textTitle,
                        style = MaterialTheme.typography.body1
                    )
                    Text(
                        text = tags[index].value,
                        color = MagicMusicTheme.colors.textContent,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}