package com.franzliszt.magicmusic.route.searchresult.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultAlbumViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    /**
     * 获取搜索结果中的专辑部分*/
    fun getSearchAlbumResult(keywords:String) = creator { offset, limit ->
        val response = service.getSearchAlbumResult(keywords = keywords,offset = offset*limit,limit = limit)
        response.result.albums
    }.flow.cachedIn(viewModelScope)
}