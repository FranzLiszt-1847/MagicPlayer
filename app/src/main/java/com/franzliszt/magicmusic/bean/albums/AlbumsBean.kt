package com.franzliszt.magicmusic.bean.albums

import com.franzliszt.magicmusic.bean.artist.Artist

data class AlbumsBean(
    val alias: List<Any>,
    val artist: Artist,
    val artists: Any,
    val awardTags:Any,
    val blurPicUrl: String,
    val briefDesc: String,
    val commentThreadId: String,
    val company: String,
    val companyId: Int,
    val copyrightId: Int,
    val description: String,
    val gapless:Int,
    val id: Long,
    val idStr:String,
    val isSub:Any,
    val info:AlbumInfo,
    val mark:Long,
    val name: String,
    val onSale: Boolean,
    val paid: Boolean,
    val pic: Long,
    val picId: Long,
    val picId_str: String,
    val picUrl: String,
    val publishTime: Long,
    val size: Int,
    val songs: Any,
    val status: Int,
    val subType:String,
    val tags: String,
    val type: String,
    val transName:Any,
    val lastSong:Any
)