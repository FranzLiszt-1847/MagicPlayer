package com.franzliszt.magicmusic.route.login.qrcode

import android.annotation.SuppressLint
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.franzliszt.magicmusic.R
import com.franzliszt.magicmusic.navigation.Screen
import com.franzliszt.magicmusic.tool.TopTitleBar
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.systemBarsPadding
import kotlinx.coroutines.flow.collectLatest

@SuppressLint( "UnusedMaterialScaffoldPaddingParameter")
@Composable
fun QrCodeLoginPage(
    viewModel: QrCodeViewModel = hiltViewModel(),
    onBack:()->Unit,
    onNavigation:()->Unit
){
    val status = viewModel.bitmapStatus.value
    val scaffoldState = rememberScaffoldState()
    val rotationValue by animateFloatAsState(
        targetValue = if (status.refresh) 360f else 0f,
        label = "Refresh",
        animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
    )

    LaunchedEffect(key1 = viewModel.eventFlow){
        viewModel.eventFlow.collectLatest {
            when(it){
                is QRCodeLoginStatus.Expire-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is QRCodeLoginStatus.Waiting-> {
                    //scaffoldState.snackbarHostState.showSnackbar(it.msg)
                }
                is QRCodeLoginStatus.Confirm-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is QRCodeLoginStatus.Success-> {
                    //scaffoldState.snackbarHostState.showSnackbar(it.msg)
                    onNavigation()
                }
                is QRCodeLoginStatus.NoCookie-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is QRCodeLoginStatus.RefreshQRCode-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
                is QRCodeLoginStatus.NetworkFailed-> scaffoldState.snackbarHostState.showSnackbar(it.msg)
            }
        }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = it) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MagicMusicTheme.colors.backgroundTop,
                            MagicMusicTheme.colors.backgroundBottom,
                            MagicMusicTheme.colors.backgroundEnd
                        )
                    )
                )
                .systemBarsPadding()
        ) {
            TopTitleBar(
                title = "二维码登录",
                onBack = onBack,
                titleBarBg = Color.Transparent,
                iconTint = MagicMusicTheme.colors.textTitle,
                modifier = Modifier.padding(top = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.magicmusic_logo),
                contentDescription = "MagicMusicLogo",
                alignment = Alignment.Center,
                modifier = Modifier.size(150.dp)
            )
            Surface(
                color = MagicMusicTheme.colors.backgroundTop.copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(50.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    val (qrcode,tips,rotate) = createRefs()
                    AsyncImage(
                        model = status.bitmap,
                        contentDescription = "QRCode",
                        placeholder = painterResource(id = R.drawable.icon_default_pic),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .fillMaxSize()
                            .constrainAs(qrcode) {
                                top.linkTo(parent.top)
                                bottom.linkTo(rotate.top,10.dp)
                                start.linkTo(parent.start, 10.dp)
                                end.linkTo(parent.end, 10.dp)
                            }
                    )
//                    Image(
//                        contentDescription = "QRCode",
//                        painter = painterResource(id = R.drawable.icon_default),
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(20.dp))
//                            .fillMaxWidth()
//                            .constrainAs(qrcode) {
//                                top.linkTo(parent.top)
//                                bottom.linkTo(rotate.top)
//                                start.linkTo(parent.start, 10.dp)
//                                end.linkTo(parent.end, 10.dp)
//                            }
//                    )
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MagicMusicTheme.colors.defaultIcon,
                        modifier = Modifier
                            .clickable { viewModel.refreshQRCode() }
                            .size(32.dp)
                            .graphicsLayer { rotationX = rotationValue }
                            .constrainAs(rotate) {
                                bottom.linkTo(tips.top,10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                    Text(
                        text = "请扫描上方二维码完成登录验证",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .constrainAs(tips) {
                                bottom.linkTo(parent.bottom, 10.dp)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                    )
                }
            }
        }
    }
}