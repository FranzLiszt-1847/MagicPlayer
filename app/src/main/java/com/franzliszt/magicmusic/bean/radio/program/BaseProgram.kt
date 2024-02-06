package com.franzliszt.magicmusic.bean.radio.program

data class BaseProgram<T>(
    val code:Int,
    val toplist:T,
    val updateTime:Long
)
