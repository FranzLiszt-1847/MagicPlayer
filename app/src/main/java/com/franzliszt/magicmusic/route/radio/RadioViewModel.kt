package com.franzliszt.magicmusic.route.radio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.parm.Constants
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadioViewModel @Inject constructor(
    private val service: MusicApiService,
    private val musicServiceHandler: MusicServiceHandler,
    private val musicUseCase: MusicUseCase,
    savedStateHandle: SavedStateHandle
):ViewModel() {
    private var radioId = 0L

    val maxTopBarHeight: Dp
        @Composable
        get() = (LocalConfiguration.current.screenHeightDp * 0.4).dp

    val minTopBarHeight = 70.dp

    private val _uiStatus = mutableStateOf(RadioUIStatus())
    val uiStatus:State<RadioUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<RadioStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        savedStateHandle.get<Long>(Constants.RadioStationID)?.let {
            radioId = it
        }
        if (radioId != 0L){
            viewModelScope.launch(Dispatchers.IO){
                getRadioStationDetail(radioId)
            }
        }
    }

    fun playProgramItem(id:Long){
        viewModelScope.launch {
            if (id != musicServiceHandler.getCurrentSongId()){
                musicUseCase.deleteAll()
                _uiStatus.value.programs.map {
                    SongMediaBean(
                        createTime = System.currentTimeMillis(),
                        songID = it.id,
                        songName = it.name,
                        cover = it.coverUrl,
                        artist = it.dj.nickname,
                        url = "",
                        isLoading = false,
                        duration = 0L,
                        size = ""
                    )
                }.also {
                    musicServiceHandler.setMediaItems(it)
                    musicUseCase.insertAll(it)
                }
            }
            musicServiceHandler.onEvent(AudioPlayerEvent.Group(id))
        }
    }
    /**
     * 获取电台详情*/
    private suspend fun getRadioStationDetail(id:Long){
        when(val response = baseApiCall { service.getRadioStationDetail(id) }){
            is RemoteResult.Success->{
                _uiStatus.value = uiStatus.value.copy(detail = response.data.data)
                getRadioPrograms(id,response.data.data.programCount)
            }
            is RemoteResult.Error->{
                _eventFlow.emit(RadioStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }

    /**
     * 获取电台所有节目*/
    private suspend fun getRadioPrograms(id: Long,limit:Int){
        when(val response = baseApiCall { service.getRadioPrograms(rid = id, limit = limit) }){
            is RemoteResult.Success->{
                _uiStatus.value = uiStatus.value.copy(programs = response.data.programs)
            }
            is RemoteResult.Error->{
                _eventFlow.emit(RadioStatus.NetworkFailed(response.exception.message.toString()))
            }
        }
    }
}