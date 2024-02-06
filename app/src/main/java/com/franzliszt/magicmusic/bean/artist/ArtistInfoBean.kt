package com.franzliszt.magicmusic.bean.artist

data class ArtistInfoBean(
    val albumSize: Int,
    val alias: List<String>,
    val avatar: String,
    val briefDesc: String,
    val cover: String,
    val id: Int,
    val identifyTag: List<String>,
    val identities: List<String>,
    val musicSize: Int,
    val mvSize: Int,
    val name: String,
    val rank: Any,
    val transNames: Any
)