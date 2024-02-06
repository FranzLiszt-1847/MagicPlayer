package com.franzliszt.magicmusic.bean.radio.program

data class NewHotRadioBean(
    val category: String,
    val categoryId: Int,
    val createTime: Long,
    val creatorName: String,
    val dj: Any,
    val feeScope: Int,
    val id: Long,
    val lastRank: Int,
    val name: String,
    val picUrl: String,
    val playCount: Long,
    val programCount: Int,
    val radioFeeType: Int,
    val rank: Int,
    val rcmdtext: String,
    val score: Int,
    val subCount: Int
)