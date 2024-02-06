package com.franzliszt.magicmusic.bean.radio

data class BaseRadioStation<T>(
    val code:Int,
    val djRadios:T,
    val hasMore:Boolean
)
