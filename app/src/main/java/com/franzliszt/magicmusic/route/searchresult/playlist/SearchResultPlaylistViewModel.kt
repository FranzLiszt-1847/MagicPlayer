package com.franzliszt.magicmusic.route.searchresult.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultPlaylistViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    /**
     * 获取搜索结果中的歌单部分*/
    fun getSearchPlaylistResult(keywords:String) = creator { offset, limit ->
        val response = service.getSearchPlaylistResult(keywords = keywords,offset = offset*limit,limit = limit)
        response.result.playlists
    }.flow.cachedIn(viewModelScope)
}