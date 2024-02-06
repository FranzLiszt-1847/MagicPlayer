package com.franzliszt.magicmusic.route.nav.radiostation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.APP
import com.franzliszt.magicmusic.bean.banner.BannerBean
import com.franzliszt.magicmusic.bean.radio.ProgramDetailBean
import com.franzliszt.magicmusic.bean.radio.RecommendRadioBean
import com.franzliszt.magicmusic.bean.radio.program.NewHotRadioBean
import com.franzliszt.magicmusic.bean.radio.program.ProgramRankBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.network.RemoteResult
import com.franzliszt.magicmusic.network.baseApiCall
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.route.playlist.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RadioStationViewModel @Inject constructor(
    private val service: MusicApiService,
    private val musicServiceHandler: MusicServiceHandler
):ViewModel()  {

    val banners = mutableStateListOf<BannerBean>()

    val recommends = mutableStateListOf<RecommendRadioBean>()

    val hots = mutableStateListOf<RecommendRadioBean>()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getRadioStationBanner()
            getRecommendRadioStation()
            getHotRadioStation()
        }
    }

    fun playProgram(bean: ProgramRankBean){
        viewModelScope.launch {
            musicServiceHandler.isExistPlaylist(
                SongMediaBean(
                    createTime = System.currentTimeMillis(),
                    songID = bean.program.id,
                    songName = bean.program.name,
                    cover = bean.program.blurCoverUrl,
                    artist = bean.program.dj.nickname,
                    url = "",
                    isLoading = false,
                    duration = 0L,
                    size = ""
                )
            )
        }
    }
    /**
     * 可获取电台 banner*/
    private suspend fun getRadioStationBanner(){
        val response = baseApiCall { service.getRadioStationBanner(APP.cookie) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.data != null && response.data.code == 200){
                    banners.addAll(response.data.data)
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
     * 可获取电台个性推荐列表*/
    private suspend fun getRecommendRadioStation(){
        val response = baseApiCall { service.getRecommendRadioStation(APP.cookie) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.data != null && response.data.code == 200){
                    recommends.addAll(response.data.data)
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
     * 可获取热门电台*/
    private suspend fun getHotRadioStation(){
        val response = baseApiCall { service.getHotRadioStation(cookie = APP.cookie) }
        when(response){
            is RemoteResult.Success->{
                if (response.data.djRadios != null && response.data.code == 200){
                    hots.addAll(response.data.djRadios)
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
     * 可获得电台节目榜
     * 里面item为节目-歌曲*/
    fun getProgramRanking() = creator { offset, limit ->
        val response = service.getProgramRanking(offset = offset*limit,limit = limit)
        response.toplist
    }.flow.cachedIn(viewModelScope)



    /**
     * 新晋电台榜
     * 里面item为电台-歌单*/
    fun getNewProgramRanking() = creator { offset, limit ->
        val response = service.getNewHotProgramRanking(type = "new",offset = offset*limit,limit = limit)
        response.toplist
    }.flow.cachedIn(viewModelScope)


    /**
     * 热门电台榜
     * 里面item为电台-歌单*/
    fun getHotProgramRanking() = creator { offset, limit ->
        val response = service.getNewHotProgramRanking(type = "hot",offset = offset*limit,limit = limit)
        response.toplist
    }.flow.cachedIn(viewModelScope)


}