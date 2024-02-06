package com.franzliszt.magicmusic.bean.albums

data class DigitAlbumsBean(
    val albumId: Int,
    val albumName: String,
    val albumType: Int,
    val area: Int,
    val artistName: String,
    val artistType: Int,
    val coverUrl: String,
    val customPriceConfig: Any,
    val newAlbum: Boolean,
    val price: Double,
    val productId: Int,
    val pubTime: Long,
    val saleNum: Int,
    val saleType: Int,
    val status: Int,
    val topfans: Any
)