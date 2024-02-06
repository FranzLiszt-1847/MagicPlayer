package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.dj.DjRadioBean

/**
 * 电台、广播*/
data class SearchDjBean(
    val djRadios: List<DjRadioBean>,
    val djRadiosCount: Int,
    val searchQcReminder: Any
)