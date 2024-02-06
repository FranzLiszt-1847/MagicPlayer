package com.franzliszt.magicmusic.bean.albums

data class AlbumInfo(
    val commentCount: Long,
    val commentThread: Any,
    val comments: Any,
    val latestLikedUsers: Any,
    val liked: Boolean,
    val likedCount: Long,
    val resourceId: Int,
    val resourceType: Int,
    val shareCount: Long,
    val threadId: String
)