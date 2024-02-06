package com.franzliszt.magicmusic.bean.recent

data class RecentMlogBean(
    val coverUrl: String,
    val creator: MlogCreator,
    val duration: Int,
    val id: String,
    val title: String
)