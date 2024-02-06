package com.franzliszt.magicmusic.route.nav.recommend.songs

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.recommend.newsongs.Result
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
    private val service: MusicApiService,
    private val musicServiceHandler: MusicServiceHandler
) : ViewModel() {
    private val _uiStatus = mutableStateOf(SongUIStatus())
    val uiStatus: State<SongUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()



    init {
        viewModelScope.launch {
            getRecommendSongs()
            getRecommendEveryDaySongs()
        }
    }

    fun onEvent(event: SongEvent) {
        viewModelScope.launch {
            when (event) {
                is SongEvent.InsertDaySong -> {
                        musicServiceHandler.isExistPlaylist(
                            SongMediaBean(
                                createTime = System.currentTimeMillis(),
                                songID = event.bean.id,
                                songName = event.bean.name,
                                cover = event.bean.al.picUrl,
                                artist = event.bean.ar[0].name,
                                url = "",
                                isLoading = false,
                                duration = 0L,
                                size = ""
                            )
                        )
                }

                is SongEvent.InsertNewSong -> {
                        musicServiceHandler.isExistPlaylist(
                            SongMediaBean(
                                createTime = System.currentTimeMillis(),
                                songID = event.bean.id,
                                songName = event.bean.name,
                                cover = event.bean.picUrl,
                                artist = event.bean.song.artists[0].name,
                                url = "",
                                isLoading = false,
                                duration = 0L,
                                size = ""
                            )
                        )
                }
            }
        }
    }

    /**
     * 获取推荐的歌曲*/
    private suspend fun getRecommendSongs(){
        val response = baseApiCall { service.getRecommendSongs() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.result != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        newSongs = response.data.result
                    )
                }else{
                    _eventFlow.emit("The error code is ${response.data.code}")
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(response.exception.message.toString())
            }
        }
    }

    /**
     * 获取推荐的新的音乐*/
    private suspend fun getRecommendEveryDaySongs() {
        val response = baseApiCall { service.getRecommendEveryDaySongs() }
        when(response){
            is RemoteResult.Success->{
                if (response.data.data != null && response.data.code == 200){
                    _uiStatus.value = uiStatus.value.copy(
                        daySongs = response.data.data.dailySongs
                    )
                }else{
                    _eventFlow.emit("The error code is ${response.data.code}")
                }
            }
            is RemoteResult.Error->{
                _eventFlow.emit(response.exception.message.toString())
            }
        }
    }
}