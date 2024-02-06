package com.franzliszt.magicmusic.bean.comment

data class BaseCommentBean(
    val cnum: Int,
    val code: Int,
    val commentBanner: Any,
    val comments: List<CommentBean>,
    val hotComments: List<CommentBean>,
    val isMusician: Boolean,
    val more: Boolean,
    val moreHot: Boolean,
    val topComments: List<CommentBean>,
    val total: Long,
    val userId: Long
)