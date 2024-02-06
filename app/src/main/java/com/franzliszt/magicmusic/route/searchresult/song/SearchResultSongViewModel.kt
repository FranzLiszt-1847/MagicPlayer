package com.franzliszt.magicmusic.route.searchresult.song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultSongViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    /**
     * 获取搜索结果中的单曲部分*/
    fun getSearchSongResult(keywords:String) = creator { offset, limit ->
        val response = service.getSearchSongResult(keywords = keywords,offset = offset*limit,limit = limit)
        response.result.songs
    }.flow.cachedIn(viewModelScope)
}