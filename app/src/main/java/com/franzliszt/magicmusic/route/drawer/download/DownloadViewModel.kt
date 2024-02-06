package com.franzliszt.magicmusic.route.drawer.download

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arialyy.aria.core.Aria
import com.arialyy.aria.core.common.AbsEntity
import com.arialyy.aria.core.download.DownloadTaskListener
import com.arialyy.aria.core.inf.IEntity
import com.arialyy.aria.core.task.DownloadTask
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.download.DownloadMusicBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadHandler
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadListener
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadService
import com.franzliszt.magicmusic.route.drawer.download.service.DownloadStateFlow
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.ui.theme.MagicMusicTheme
import com.franzliszt.magicmusic.ui.theme.green400
import com.franzliszt.magicmusic.ui.theme.grey
import com.franzliszt.magicmusic.ui.theme.grey200
import com.franzliszt.magicmusic.ui.theme.red100
import com.franzliszt.magicmusic.usecase.download.DownloadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadHandler: DownloadHandler,
    private val musicServiceHandler: MusicServiceHandler
) : ViewModel() {

    var downloadList = mutableStateListOf<DownloadMusicBean>()

    private val _dialogState = mutableStateOf(DownloadDialogState())
    val dialogState:State<DownloadDialogState> = _dialogState
    

    init {
        downloadListener()
        downloadList = downloadHandler.getCurrentDownloads().toMutableStateList()
    }

    /**
     * 监听下载状态并即使变更到UI*/
    private fun downloadListener(){
        viewModelScope.launch {
            downloadHandler.eventFlow.collectLatest { state->
                when(state){
                    is DownloadStateFlow.Prepare->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = "waiting",
                            progressColor = ProgressColor.Waiting.color,
                            taskID = state.task.entity.id
                        )
                    }
                    is DownloadStateFlow.Running->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = state.task.convertSpeed ?: "0kb",
                            progressColor = ProgressColor.Running.color,
                            progress = (state.task.currentProgress * 100 / state.task.fileSize).toFloat()
                        )
                    }
                    is DownloadStateFlow.Stop->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = "stop",
                            progressColor = ProgressColor.Stop.color
                        )
                    }
                    is DownloadStateFlow.Complete->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            download = true
                        )
                    }
                    is DownloadStateFlow.Fail->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = "fail",
                            progressColor = ProgressColor.Fail.color
                        )
                    }
                    is DownloadStateFlow.Cancel->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = "cancel"
                        )
                        downloadList.removeAt(state.index)
                    }
                    is DownloadStateFlow.Other->{
                        downloadList[state.index] = downloadList[state.index].copy(
                            speed = "waiting",
                            progressColor = ProgressColor.Waiting.color
                        )
                    }

                    is DownloadStateFlow.PermissionDenied->{

                    }
                }
            }
        }
    }

    fun onEvent(event: DownloadEvent){
        viewModelScope.launch {
            when(event){
                is DownloadEvent.ShowDialog->{
                    _dialogState.value = dialogState.value.copy(
                        isVisibility = true
                    )
                }

                is DownloadEvent.DialogCancel->{
                    _dialogState.value = dialogState.value.copy(
                        isVisibility = false
                    )
                }

                is DownloadEvent.DialogConfirm->{
                    _dialogState.value = dialogState.value.copy(
                        isVisibility = false
                    )
                    downloadList.clear()
                    downloadHandler.clearAllRecords() //删除Aria数据库和本地对应文件
                }

                /**
                 * 切换下载状态，例如下载ing->暂停、暂停(下载失败)->恢复下载
                 * */
                is DownloadEvent.PlayOrPause->{
                    if(event.bean.taskID >0){
                        val isRunning = Aria.download(this).load(event.bean.taskID).isRunning
                        if (isRunning){
                            Aria.download(this).load(event.bean.taskID).stop()
                        }else{
                            Aria.download(this).load(event.bean.taskID).resume()
                        }
                    }
                }

                is DownloadEvent.PlayLocalMusic->{
                    /**
                     * 播放已经下载好的资源，即本地文件*/
                    musicServiceHandler.playLocalMusic(
                        SongMediaBean(
                            createTime = System.currentTimeMillis(),
                            songID = event.bean.musicID,
                            songName = event.bean.musicName,
                            cover = event.bean.cover,
                            artist = event.bean.artist,
                            url = event.bean.path,
                            isLoading = true,
                            duration = 0L,
                            size = event.bean.size
                        )
                    )
                }
            }
        }
    }
}

private enum class ProgressColor(val color: Color) {
    Waiting(grey),
    Running(green400),
    Stop(grey),
    Fail(red100)
}
