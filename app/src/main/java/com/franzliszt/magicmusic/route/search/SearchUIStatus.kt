package com.franzliszt.magicmusic.route.search

import com.franzliszt.magicmusic.bean.search.HotSearch
import com.franzliszt.magicmusic.bean.search.SearchRecordBean
import com.franzliszt.magicmusic.bean.search.SearchSuggestionBean

data class SearchUIStatus(
    val keywords:String = "",
    val default:String = "Please enter some keyword...",
    val hots:List<HotSearch> = emptyList(),
    val suggestions: SearchSuggestionBean? = null,
    val isEmptySuggestions:Boolean = true,
    val isShowClear:Boolean = false,
    val isShowDialog:Boolean = false,
    val histories:List<SearchRecordBean> = emptyList()
)
