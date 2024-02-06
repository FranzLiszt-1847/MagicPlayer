package com.franzliszt.magicmusic.route.searchresult.dj

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.franzliszt.magicmusic.datapaging.creator
import com.franzliszt.magicmusic.network.MusicApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchResultDjViewModel @Inject constructor(private val service: MusicApiService):ViewModel() {
    /**
     * 获取搜索结果中的电台部分*/
    fun getSearchDjResult(keywords:String) = creator { offset, limit ->
        val response = service.getSearchDjResult(keywords = keywords,offset = offset*limit,limit = limit)
        response.result.djRadios
    }.flow.cachedIn(viewModelScope)
}