package com.franzliszt.magicmusic.bean.recommend.playlist

data class RecommendPlaylistBean(
    val category: Int,
    val code: Int,
    val hasTaste: Boolean,
    val result: List<Result>
)