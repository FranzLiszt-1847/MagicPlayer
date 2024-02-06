package com.franzliszt.magicmusic.route.nav.recommend.artist

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.network.MusicApiService
import com.franzliszt.magicmusic.datapaging.creator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {

    private val _uiStatus = mutableStateOf(ArtistUIStatus())
    val uiStatus:State<ArtistUIStatus> = _uiStatus

    private val _eventFlow = MutableSharedFlow<ArtistStatus>()
    val eventFlow = _eventFlow.asSharedFlow()

    private  val PAGE_SIZE = 20


    fun onEvent(event:ArtistEvent){
        when(event){
            is ArtistEvent.Retry->{
                viewModelScope.launch { _eventFlow.emit(ArtistStatus.Retry) }
            }
            is ArtistEvent.Finish->{
                viewModelScope.launch { _eventFlow.emit(ArtistStatus.Finish) }
            }
        }
    }

    fun getArtists() = creator{ offset,limit->
        val response = service.getAllArtists(offset = offset*limit, limit = limit)
        response.artists
    }.flow.cachedIn(viewModelScope)
}