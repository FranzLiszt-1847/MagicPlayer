package com.franzliszt.magicmusic

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.arialyy.aria.core.Aria
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadHandler
import com.franzliszt.magicmusic.route.musicplayer.MusicPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.MusicPlayerViewModel
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.usecase.download.DownloadUseCase
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var musicServiceHandler:MusicServiceHandler

    @Inject
    lateinit var downloadHandler:DownloadHandler


    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window,false)
        super.onCreate(savedInstanceState)
        init()
        setContent {
            val permissionState = rememberMultiplePermissionsState(permissions = listOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ))
            ApplyForMultiPermissions(
                permissionState = permissionState,
                onPermission = {
                    APP.permissions = it
            })
            MagicMusicApp()
        }
    }
    private fun init(){
        //初始化歌单
        musicServiceHandler.initPlaylist()
        //注册下载管理器
        Aria.init(this)

        //初始化下载器
        downloadHandler.register()
        parentThis = this
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadHandler.unRegister()
    }
    companion object{
        lateinit var parentThis:MainActivity

        fun Context.setScreenOrientation(orientation: Int) {
            val activity = this.findActivity() ?: return
            activity.requestedOrientation = orientation
            if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                hideSystemUi()
            } else {
                showSystemUi()
            }
        }

        private fun Context.hideSystemUi() {
            val activity = this.findActivity() ?: return
            val window = activity.window ?: return
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        private fun Context.showSystemUi() {
            val activity = this.findActivity() ?: return
            val window = activity.window ?: return
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).show(WindowInsetsCompat.Type.systemBars())
        }

        private fun Context.findActivity(): Activity? = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.findActivity()
            else -> null
        }
    }
}
