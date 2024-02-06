package com.franzliszt.magicmusic.route.searchresult.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultVideoViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    /**
     * 获取搜索结果中的视频部分*/
    fun getSearchVideoResult(keywords:String) = creator { offset, limit ->
        val response = service.getSearchVideoResult(keywords = keywords,offset = offset*limit,limit = limit)
        response.result.videos
    }.flow.cachedIn(viewModelScope)
}