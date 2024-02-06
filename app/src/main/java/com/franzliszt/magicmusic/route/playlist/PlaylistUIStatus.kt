package com.franzliszt.magicmusic.route.playlist

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.franzliszt.magicmusic.bean.comment.CommentBean
import com.franzliszt.magicmusic.bean.recommend.songs.DailySong

data class PlaylistUIStatus(
    val id:Long = 0L,
    val songs:List<DailySong> = emptyList(),
    val cover:String = "",
    val name:String = "Unknown",
    val description:String = "Unknown",
    val artist:String = "Unknown",
    val shareCount:Long = 0L,
    val favoriteCount:Long = 0L,
    val commentCount:Long = 0L,
    val isPlaylist:Boolean = false,
    val isShowDialog:Boolean = false,
    val isFollow:Boolean = false,
    val tags:List<String> = emptyList(),
    val company:String = "Unknown",
    val type:String = "Unknown",
)

data class CommentUIStatus(
    val commentCount:Long = 0L,
    val floorCommentCount:Long = 0L,
    val commentStatus:NetworkStatus = NetworkStatus.Waiting,
    val floorCommentStatus:NetworkStatus = NetworkStatus.Waiting,
    val ownFloorComment:CommentBean? = null,
    val floorComments:MutableList<CommentBean> = mutableListOf(),
    val comments:MutableList<CommentBean> = mutableListOf(),
    val commentText:String = "",
    val floorCommentText:String = ""
)
