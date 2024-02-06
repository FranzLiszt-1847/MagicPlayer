package com.franzliszt.magicmusic.bean.albums

data class BaseProduct<T> (
    val code:Int,
    val products:T
)