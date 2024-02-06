package com.franzliszt.magicmusic.bean.searchresult

import com.franzliszt.magicmusic.bean.mv.MvBean

data class SearchMvBean(
    val code:Int,
    val mvCount: Int,
    val mvs: List<MvBean>
)