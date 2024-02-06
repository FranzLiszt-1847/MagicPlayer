package com.franzliszt.magicmusic.bean

data class BaseResponse<T>(
    val data:T,
    val code:Int,
    val hasMore:Any,
    val message:Any,
    val msg:Any,
    val count:Int,
    val paidCount:Int
)
