package com.franzliszt.magicmusic.bean.radio.program

data class ProgramRankBean(
    val lastRank: Int,
    val program: Program,
    val programFeeType: Int,
    val rank: Int,
    val score: Int
)