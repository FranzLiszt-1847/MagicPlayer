package com.franzliszt.magicmusic.bean.mv

data class MvUrlBean(
    val code: Int,
    val expi: Int,
    val fee: Int,
    val id: Int,
    val md5: String,
    val msg: String,
    val mvFee: Int,
    val promotionVo: Any,
    val r: Int,
    val size: Int,
    val st: Int,
    val url: String
)