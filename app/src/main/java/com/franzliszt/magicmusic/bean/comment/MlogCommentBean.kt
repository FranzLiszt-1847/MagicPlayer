package com.franzliszt.magicmusic.bean.comment

data class MlogCommentBean(
    val bottomAction: Any,
    val comments: List<CommentBean>,
    val commentsTitle: String,
    val currentComment: Any,
    val currentCommentTitle: String,
    val cursor: String,
    val hasMore: Boolean,
    val likeAnimation: Any,
    val sortType: Int,
    val sortTypeList: List<Any>,
    val style: String,
    val totalCount: Long
)