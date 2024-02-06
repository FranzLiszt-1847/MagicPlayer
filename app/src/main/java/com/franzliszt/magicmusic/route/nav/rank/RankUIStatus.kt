package com.franzliszt.magicmusic.route.nav.rank

import com.franzliszt.magicmusic.bean.rank.Rankbean

data class RankUIStatus(
    val official:MutableList<Rankbean> = mutableListOf(),
    val global:MutableList<Rankbean> = mutableListOf()
)
