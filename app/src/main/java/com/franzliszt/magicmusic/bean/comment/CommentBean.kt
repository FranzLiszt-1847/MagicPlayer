package com.franzliszt.magicmusic.bean.comment

data class CommentBean(
    val beRepliedUser:Any?,
    val beReplied: Any?,
    val commentId: Long,
    val commentLocationType: Int,
    val content: String,
    val contentResource: Any?,
    val decoration: Any?,
    val expressionUrl: Any?,
    val grade: Any?,
    val ipLocation: Any?,
    val liked: Boolean,
    val likedCount: Int,
    val needDisplayTime: Boolean,
    val owner: Boolean,
    val parentCommentId: Long,
    val pendantData: Any?,
    val repliedMark: Any?,
    val richContent: Any?,
    val showFloorComment: Any?,
    val status: Int,
    val time: Long,
    val timeStr: String?,
    val user: CommentUserBean,
    val userBizLevels: Any?
)