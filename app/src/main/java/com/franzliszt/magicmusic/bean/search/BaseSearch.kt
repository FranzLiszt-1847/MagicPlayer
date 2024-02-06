package com.franzliszt.magicmusic.bean.search

data class BaseSearch<T>(
    val code:Int,
    val result:T
)
