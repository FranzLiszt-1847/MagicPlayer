package com.franzliszt.magicmusic.route.searchresult

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.song.SongMediaBean
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.route.musicplayer.service.AudioPlayerEvent
import com.franzliszt.magicmusic.route.musicplayer.service.MusicServiceHandler
import com.franzliszt.magicmusic.route.search.SearchEvent
import com.franzliszt.magicmusic.route.search.SearchStatus
import com.franzliszt.magicmusic.usecase.search.SearchUseCase
import com.franzliszt.magicmusic.usecase.song.MusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val useCase:SearchUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val musicServiceHandler: MusicServiceHandler
):ViewModel() {
    private val _uiStatus = mutableStateOf(SearchResultUIStatus())
    val uiStatus:State<SearchResultUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<SearchStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

//    private var musics: List<SongMediaBean> = emptyList()
//    private var job: Job? = null
    init {
        viewModelScope.launch {
            savedStateHandle.get<String>("key")?.let {
                if (it.isNotEmpty()){
                    _uiStatus.value = uiStatus.value.copy(
                        keyword = it,
                        buffer = it
                    )
                }
            }
           // getMusics()
        }
    }

    fun onEvent(event:SearchEvent){
        viewModelScope.launch(Dispatchers.Main){
            when(event){
                is SearchEvent.Search->{
                    if (_uiStatus.value.buffer.isEmpty()){
                        _eventFlow.emit(SearchStatus.SearchEmpty)
                    }else{
                        _uiStatus.value = uiStatus.value.copy(
                            keyword = _uiStatus.value.buffer,
                        )
                        useCase.insert(SearchRecordBean(
                            createTime = System.currentTimeMillis(),
                            keyword = _uiStatus.value.keyword
                        ))
                    }
                }
                is  SearchEvent.ChangeKey->{
                    _uiStatus.value = uiStatus.value.copy(
                        buffer = event.key
                    )
                }

                is SearchEvent.InsertMusicItem->{
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
                else->{}
            }
        }
    }

//    private suspend fun insert(bean: SongMediaBean) {
//        if (musics.isEmpty() || (musics.find { it.songID == bean.songID } == null)) {
//            musicUseCase.insert(bean)
//            musicServiceHandler.onEvent(AudioPlayerEvent.Singer(bean,true))
//        }else{
//            musicServiceHandler.onEvent(AudioPlayerEvent.Singer(bean,false))
//        }
//    }
//
//    private fun getMusics() {
//        job?.cancel()
//        job = musicUseCase.queryAll().onEach {
//            musics = it
//        }.launchIn(viewModelScope)
//    }
}